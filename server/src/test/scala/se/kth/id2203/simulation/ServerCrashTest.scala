package se.kth.id2203.simulation

import org.scalatest.{FlatSpec, Matchers}
import se.sics.kompics.simulator.result.SimulationResultSingleton
import se.sics.kompics.simulator.run.LauncherComp
import se.sics.kompics.sl.simulator.SimulationResult
import se.sics.kompics.simulator.{SimulationScenario => JSimulationScenario}

class ServerCrashTest extends FlatSpec with Matchers {
  private val nMessages = 3;
  private val serverNum = 4;
  "single server crash" should "has no effect on the system" in {
    val seed = 123l;
    JSimulationScenario.setSeed(seed);
    val simpleBootScenario = ServerCrashScenario.scenario(serverNum);

    val res = SimulationResultSingleton.getInstance();

    SimulationResult += ("messages" -> nMessages);

    simpleBootScenario.simulate(classOf[LauncherComp]);

    //verify simple operations should still work
    for (i <- 0 to nMessages) {
      //PUT
      SimulationResult.get[String](s"test$i") should be(Some(s"value$i"));

    }

    for(i <- 0 to nMessages){
      //GET
      SimulationResult.get[String](s"testCase$i") should be(Some(s"testValue$i"));
    }

    for(i <- 0 to nMessages) {
      //CAS
      SimulationResult.get[String](s"TestCase$i") should be(Some(s"NewTestValue$i"));
    }
  }

}