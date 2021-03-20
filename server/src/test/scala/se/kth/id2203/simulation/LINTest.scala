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
import se.sics.kompics.simulator.result.SimulationResultSingleton
import se.sics.kompics.simulator.run.LauncherComp
import se.sics.kompics.simulator.{SimulationScenario => JSimulationScenario}
import se.sics.kompics.sl.simulator._

class LINTest extends FlatSpec with Matchers {

  private val nMessages = 3;
  private val serverNum = 4;

  "system" should "linearlizable" in {
    val seed = 123l;
    JSimulationScenario.setSeed(seed);
    val simpleBootScenario = LINScenario.scenario(serverNum);
    val res = SimulationResultSingleton.getInstance();

    simpleBootScenario.simulate(classOf[LauncherComp]);


    //SimulationResult.get[String](s"LINTest") should be(Some(s"test"));
    SimulationResult.get[String](s"lin1") should be(Some(s"linValue"));//put
    SimulationResult.get[String](s"lin2") should be(Some(s"newLinValue"));//cas
    SimulationResult.get[String](s"lin3") should be(Some(s"newLinValue"));//get
    SimulationResult.get[String](s"lin4") should be(Some(s"linValue_2"));//put
    SimulationResult.get[String](s"lin5") should be(Some(s"linValue_2"));//get
    SimulationResult.get[String](s"lin6") should be(Some(s"newLinValue"));//get
  }

}





