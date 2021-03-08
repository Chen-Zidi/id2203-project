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
package se.kth.id2203.simulation;

import java.util.UUID;
import se.kth.id2203.kvstore._;
import se.kth.id2203.networking._;
import se.kth.id2203.overlay.RouteMsg;
import se.sics.kompics.sl._
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.kompics.sl.simulator.SimulationResult;
import collection.mutable;

class ScenarioClient extends ComponentDefinition {

  //******* Ports ******
  val net = requires[Network];
  val timer = requires[Timer];
  //******* Fields ******
  val self = cfg.getValue[NetAddress]("id2203.project.address");
  val server = cfg.getValue[NetAddress]("id2203.project.bootstrap-address");
  private val pending = mutable.Map.empty[UUID, String];
  //******* Handlers ******
  ctrl uponEvent {
    case _: Start => {
      val messages = SimulationResult[Int]("messages");

      //put operation
      for (i <- 0 to messages) {
        val put = new Put(s"test$i", s"tValue$i");
        val routeMsg = RouteMsg(put.key, put); // don't know which partition is responsible, so ask the bootstrap server to forward it
        trigger(NetMessage(self, server, routeMsg) -> net);
        sendOp(put);
      }

      //get operation
      for (i <- 0 to messages) {
        val get = new Get(s"test$i");
        val routeMsg = RouteMsg(get.key, get); // don't know which partition is responsible, so ask the bootstrap server to forward it
        trigger(NetMessage(self, server, routeMsg) -> net);
        sendOp(get);
      }

      //cas operation
      for (i <- 0 to messages/2) {//cas the first 5 key-value pairs
        val cas = new Cas(s"test$i", s"tValue$i", s"newValue$i");
        val routeMsg = RouteMsg(cas.key, cas); // don't know which partition is responsible, so ask the bootstrap server to forward it
        trigger(NetMessage(self, server, routeMsg) -> net);
        sendOp(cas);
      }

    }
  }
  def sendOp(op:Operation): Unit ={
    trigger(NetMessage(self, server, op)->net);
    pending += (op.id -> op.key);
    logger.info("Sending {}", op);
    SimulationResult += (op.key -> self.toString);
  }

  net uponEvent {
//    case NetMessage(header, or @ OpResponse(id, status)) => {
//      logger.debug(s"Got OpResponse: $or");
//      pending.remove(id) match {
//        case Some(key) => SimulationResult += (key -> status.toString());
//        case None      => logger.warn("ID $id was not pending! Ignoring response.");
//      }
//    }

    case NetMessage(header, or @ PutResponse(id, status, value)) => {
      logger.debug(s"put operation response: $or");
      pending.remove(id) match {
        case Some(key) => SimulationResult += (key -> value);
        case None      => logger.warn("ID $id was not pending! Ignoring response.");
      }
    }

    case NetMessage(header, or @ GetResponse(id, status, value)) => {
      logger.debug(s"get operation response: $or");
      pending.remove(id) match {
        case Some(key) => SimulationResult += (key -> value);
        case None      => logger.warn("ID $id was not pending! Ignoring response.");
      }
    }

    case NetMessage(header, or @ CasResponse(id, status, oldValue, newValue)) => {
      logger.debug(s"cas operation response: $or");
      pending.remove(id) match {
        case Some(key) => SimulationResult += (key -> newValue);//new value here in the result
        case None      => logger.warn("ID $id was not pending! Ignoring response.");
      }
    }

  }
}
