package se.kth.id2203.simulation

import se.kth.id2203.ParentComponent
import se.kth.id2203.networking.NetAddress
import se.kth.id2203.simulation.OperationClientScenario
import se.sics.kompics.network.Address
import se.sics.kompics.simulator.network.impl.NetworkModels
import se.sics.kompics.sl.Init
import se.sics.kompics.sl.simulator.Distributions.constant
import se.sics.kompics.sl.simulator.{ChangeNetwork, Distributions, Op, StartNode, raise}
import se.sics.kompics.simulator.{SimulationScenario => JSimulationScenario}
import java.net.{InetAddress, UnknownHostException}

import org.scalatest._
import se.kth.id2203.ParentComponent;
import se.kth.id2203.networking._;
import se.sics.kompics.network.Address
import java.net.{InetAddress, UnknownHostException};
import se.sics.kompics.sl._;
import se.sics.kompics.sl.simulator._;
import se.sics.kompics.simulator.{SimulationScenario => JSimulationScenario}
import se.sics.kompics.simulator.run.LauncherComp
import se.sics.kompics.simulator.result.SimulationResultSingleton;
import se.sics.kompics.simulator.network.impl.NetworkModels
import scala.concurrent.duration._

object ServerCrashScenario {

  import Distributions._

  // needed for the distributions, but needs to be initialised after setting the seed
  implicit val random = JSimulationScenario.getRandom();


//  private def intToServerAddress(i: Int): Address = {
//    try {
//      NetAddress(InetAddress.getByName("192.193.0." + i), 45678);
//    } catch {
//      case ex: UnknownHostException => throw new RuntimeException(ex);
//    }
//  }

  val serverCrashOp = Op { (self: Integer) =>
    val selfAddr = SimpleScenario.intToServerAddress(self)
//    val conf = Map(
//      "id2203.project.address" -> selfAddr,
//      "id2203.project.bootstrap-address" -> intToServerAddress(1));
//    StartNode(selfAddr, Init.none[ScenarioClient], conf);
    KillNode(selfAddr);
  };

  def scenario(servers: Int): JSimulationScenario = {

    val networkSetup = raise(1, SimpleScenario.setUniformLatencyNetwork()).arrival(constant(0));
    val startCluster = raise(servers, SimpleScenario.startServerOp, 1.toN).arrival(constant(1.second));
    val startClients = raise(1, SimpleScenario.startClientOp, 1.toN).arrival(constant(1.second));
    val crashServer = raise(1, serverCrashOp, 2.toN).arrival(constant(1.second));

        networkSetup andThen
          0.seconds afterTermination startCluster andThen
          10.seconds afterTermination startClients andThen
          50.seconds afterTermination crashServer andThen
          100.seconds afterTermination Terminate

  }

}