package se.kth.id2203.simulation

import se.kth.id2203.ParentComponent
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.network.Address
import se.sics.kompics.simulator.{SimulationScenario => JSimulationScenario}
import se.sics.kompics.sl.Init
import se.sics.kompics.sl.simulator.{Distributions, Op, StartNode, raise, _}

import java.net.{InetAddress, UnknownHostException}
import scala.concurrent.duration._

object LINScenario {

  import Distributions._

  // needed for the distributions, but needs to be initialised after setting the seed
  implicit val random = JSimulationScenario.getRandom();

  def intToServerAddress(i: Int): Address = {
    try {
      NetAddress(InetAddress.getByName("192.193.0." + i), 45678);
    } catch {
      case ex: UnknownHostException => throw new RuntimeException(ex);
    }
  }

  def intToClientAddress(i: Int): Address = {
    try {
      NetAddress(InetAddress.getByName("192.193.1." + i), 45678);
    } catch {
      case ex: UnknownHostException => throw new RuntimeException(ex);
    }
  }

  private def isBootstrap(self: Int): Boolean = self == 1;

  //val setUniformLatencyNetwork = () => Op.apply((_: Unit) => ChangeNetwork(NetworkModels.withUniformRandomDelay(3, 7)));


  val startServerOp = Op { (self: Integer) =>

    val selfAddr = intToServerAddress(self)
    val conf = if (isBootstrap(self)) {
      // don't put this at the bootstrap server, or it will act as a bootstrap client
      Map("id2203.project.address" -> selfAddr)
    } else {
      Map(
        "id2203.project.address" -> selfAddr,
        "id2203.project.bootstrap-address" -> intToServerAddress(1))
    };
    StartNode(selfAddr, Init.none[ParentComponent], conf);
  };

  val startClientOp = Op { (self: Integer) =>
    val selfAddr = intToClientAddress(self)
    val conf = Map(
      "id2203.project.address" -> selfAddr,
      "id2203.project.bootstrap-address" -> intToServerAddress(1));

      StartNode(selfAddr, Init.none[LINClientScenario], conf);


  };

  def scenario(servers: Int): JSimulationScenario = {

    //need no random latency network
    //val networkSetup = raise(1, setUniformLatencyNetwork()).arrival(constant(0));
    val startCluster = raise(servers, startServerOp, 1.toN).arrival(constant(1.second));
    val startClients = raise(1, startClientOp, 1.toN).arrival(constant(1.second));

//    networkSetup andThen
//      0.seconds afterTermination startCluster andThen
//      10.seconds afterTermination startClients andThen
//      100.seconds afterTermination Terminate

        startCluster andThen
          10.seconds afterTermination startClients andThen
          100.seconds afterTermination Terminate
  }

}