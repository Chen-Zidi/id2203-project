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

import se.kth.id2203.kvstore._
import se.kth.id2203.networking._
import se.kth.id2203.overlay.RouteMsg
import se.sics.kompics.Start
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.sl.simulator.SimulationResult
import se.sics.kompics.timer.Timer

import java.util.UUID
import scala.collection.mutable;

class LINClientScenario extends ComponentDefinition {

  //******* Ports ******
  val net = requires[Network];
  val timer = requires[Timer];
  //******* Fields ******
  val self = cfg.getValue[NetAddress]("id2203.project.address");
  val server = cfg.getValue[NetAddress]("id2203.project.bootstrap-address");
  //private val pending = mutable.Map.empty[UUID, String];
  private val linPending = mutable.Map.empty[UUID, String];
  private val trace = mutable.Queue.empty[UUID];

  private var counter = 0;


  //******* Handlers ******
  ctrl uponEvent {
    case _: Start => {

      //lIN test operations: put cas get put get get

      val lin1 = new Put(s"lin", s"linValue");
      trace.enqueue(lin1.id);
      var routeMsg1 = RouteMsg(lin1.key, lin1);
      trigger(NetMessage(self, server, routeMsg1) -> net);
      logger.info("Sending {}", lin1);
      //SimulationResult += (put.key -> "Sent");
      linPending += (lin1.id -> "lin1");
      //pending += (lin1.id -> lin1.key);
      SimulationResult += ("lin1" -> "Sent");


      val lin2 = new Cas(s"lin", s"linValue", s"newLinValue");
      trace.enqueue(lin2.id);
      var routeMsg2 = RouteMsg(lin2.key, lin2);
      trigger(NetMessage(self, server, routeMsg2) -> net);
      logger.info("Sending {}", lin2);
      linPending += (lin2.id -> "lin2");
      //pending += (lin2.id -> lin2.key);
      SimulationResult += ("lin2" -> "Sent");

      val lin3 = new Get(s"lin");
      trace.enqueue(lin3.id);
      var routeMsg3 = RouteMsg(lin3.key, lin3);
      trigger(NetMessage(self, server, routeMsg3) -> net);
      logger.info("Sending {}", lin3);
      linPending += (lin3.id -> "lin3");
      SimulationResult += ("lin3" -> "Sent");

      val lin4 = new Put(s"lin_2","linValue_2");
      trace.enqueue(lin4.id);
      var routeMsg4 = RouteMsg(lin4.key, lin4);
      trigger(NetMessage(self, server, routeMsg4) -> net);
      logger.info("Sending {}", lin4);
      linPending += (lin4.id -> "lin4");
      SimulationResult += ("lin4" -> "Sent");

      val lin5 = new Get(s"lin_2");
      trace.enqueue(lin5.id);
      var routeMsg5 = RouteMsg(lin5.key, lin5);
      trigger(NetMessage(self, server, routeMsg5) -> net);
      logger.info("Sending {}", lin5);
      linPending += (lin5.id -> "lin5");
      SimulationResult += ("lin5" -> "Sent");

      val lin6 = new Get(s"lin");
      trace.enqueue(lin6.id);
      var routeMsg6 = RouteMsg(lin6.key, lin6);
      trigger(NetMessage(self, server, routeMsg6) -> net);
      logger.info("Sending {}", lin6);
      linPending += (lin6.id -> "lin6");
      SimulationResult += ("lin6" -> "Sent");


    }


  }


  net uponEvent {
    case NetMessage(header, or @ PutResponse(id, status, value)) => {
      logger.debug(s"put operation response: $or");

      if(linPending.contains(id)){
        SimulationResult += ( linPending.get(id).get -> value);
//        if(trace.dequeue() == id){
//          SimulationResult += ( linPending.get(id).get -> "ok");
//        }
//        counter = counter + 1;
//        SimulationResult += ("counter_put" -> counter.toString)
      }
//        pending.remove(id) match {
//          case Some(key) => SimulationResult += (key -> value);
//          case None      => logger.warn("ID $id was not pending! Ignoring response.");
//
//      }
    }

    case NetMessage(header, or @ GetResponse(id, status, value)) => {
      logger.debug(s"get operation response: $or");
      if(linPending.contains(id)){
        SimulationResult += ( linPending.get(id).get -> value);
//        if(trace.dequeue() == id){
//          SimulationResult += ( linPending.get(id).get -> "ok");
//        }
//        counter = counter + 1;
//        SimulationResult += ("counter_get" -> counter.toString)
      }
//        pending.remove(id) match {
//          case Some(key) => SimulationResult += (key -> value);
//          case None      => logger.warn("ID $id was not pending! Ignoring response.");
//
//      }

    }

    case NetMessage(header, or @ CasResponse(id, status, oldValue, newValue)) => {
      logger.debug(s"cas operation response: $or");
      if(linPending.contains(id)){
        SimulationResult += ( linPending.get(id).get -> newValue);
//        if(trace.dequeue() == id){
//          SimulationResult += ( linPending.get(id).get -> "ok");
//        }
      }
//        pending.remove(id) match {
//          case Some(key) => SimulationResult += (key -> newValue);//new value here in the result
//          case None      => logger.warn("ID $id was not pending! Ignoring response.");
//
//      }

    }

  }
}
