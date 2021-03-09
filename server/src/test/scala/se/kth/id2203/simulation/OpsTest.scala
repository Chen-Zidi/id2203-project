/*
 * The MIT License
 *
 * Copyright 2017 Lars Kroll <lkroll@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.kth.id2203.simulation

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

class OpsTest extends FlatSpec with Matchers {

  private val nMessages = 3;
  private val serverNum = 4;
  //  "Classloader" should "be something" in {
  //    val cname = classOf[SimulationResultSingleton].getCanonicalName();
  //    var cl = classOf[SimulationResultSingleton].getClassLoader;
  //    var i = 0;
  //    while (cl != null) {
  //      val res = try {
  //        val c = cl.loadClass(cname);
  //        true
  //      } catch {
  //        case t: Throwable => false
  //      }
  //      println(s"$i -> ${cl.getClass.getName} has class? $res");
  //      cl = cl.getParent();
  //      i -= 1;
  //    }
  //  }

  //  "Operations" should "be implemented" in {
  //    val seed = 123l;
  //    JSimulationScenario.setSeed(seed);
  //
  //    //val simpleBootScenario = SimpleScenario.scenario(3);
  //    val simpleBootScenario = SimpleScenario.scenario(4);
  //    val res = SimulationResultSingleton.getInstance();
  //    SimulationResult += ("messages" -> nMessages);
  //
  //
  //    simpleBootScenario.simulate(classOf[LauncherComp]);
  //
  //
  //    for (i <- nMessages/2 + 1 to nMessages) {//the later 5 key-value pairs are not being cas
  //      //PUT
  //      //SimulationResult.get[String](s"test$i") should be (Some(s"tValue$i"));
  //      SimulationResult.get[String](s"test$i") should be (Some(s"Ok"));
  //      //GET
  //      SimulationResult.get[String](s"test$i") should be (Some(s"tValue$i"));
  //    }
  //
  //    for (i <- 0 to nMessages/2) {
  //      //CAS
  //      SimulationResult.get[String](s"test$i") should be (Some(s"newValue$i"));
  //    }
  //  }

  "simple operation" should "be implemented" in {
    val seed = 123l;
    JSimulationScenario.setSeed(seed);
    val simpleBootScenario = SimpleScenario.scenario(serverNum);
    val res = SimulationResultSingleton.getInstance();
    SimulationResult += ("messages" -> nMessages);
    simpleBootScenario.simulate(classOf[LauncherComp]);

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




