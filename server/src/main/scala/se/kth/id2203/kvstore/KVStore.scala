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
package se.kth.id2203.kvstore

;

import se.kth.id2203.networking._
import se.kth.id2203.overlay.Routing
import se.kth.id2203.paxos.{SC_Decide, SC_Propose, SequenceConsensus}
import se.sics.kompics.sl._
import se.sics.kompics.network.Network

import java.util.UUID
import scala.collection.mutable
import scala.concurrent.Promise;


class KVService extends ComponentDefinition {

  //******* Ports ******
  val net = requires[Network];
  val route = requires(Routing);

  //added
  val sc = requires[SequenceConsensus];

  //added
  //stores all key-value pairs
  //initially has three values
  var data = mutable.HashMap("A" -> "Apple", "B" -> "banana", "C" -> "Cherry");
  //to store all operations
  private val pendingList = mutable.SortedMap.empty[UUID, NetAddress];
  //******* Fields ******
  val self = cfg.getValue[NetAddress]("id2203.project.address");


  //******* Handlers ******
  //  net uponEvent {
  //    case NetMessage(header, op @ Get(key, _)) => {
  //      log.info("Got operation {}! Now implement me please :)", op);
  //      trigger(NetMessage(self, header.src, op.response(OpCode.NotImplemented)) -> net);
  //    }
  //    case NetMessage(header, op @ Put(key, value, _)) => {
  //      log.info("Got operation {}! Now implement me please :)", op);
  //      trigger(NetMessage(self, header.src, op.response(OpCode.NotImplemented)) -> net);
  //    }
  //  }

  //receive the operation
  net uponEvent {
    case NetMessage(header, operation: Operation) => {
      log.info(" Got operation: {}", operation);
      pendingList += (operation.id -> header.src); //add to pending list
      trigger(SC_Propose(operation) -> sc); //propose to the sequence consensus component
    }
  }

  sc uponEvent {
    //sequence consensus decide
    case SC_Decide(operation: Operation) => {
      log.info("[KVStore - SC] Decide operation: {}", operation);
      var opSrc = self;
      if (pendingList.contains(operation.id)) {
        opSrc = pendingList.get(operation.id).get; //get the address of the operation sender
      }

      operation match {
        case Get(key, id) => { //get operation
          if (data.contains(key)) { //the key exists
            val getValue = data.get(key);
            println("[KVStore] GET operation: " + key + " - " + getValue);
            //send back the response
            trigger(NetMessage(self, opSrc, GetResponse(id, OpCode.Ok, getValue.get)) -> net);

            pendingList.remove(id);
          } else { //key not found
            println("[KVStore] GET operation error: key " + key + " not found");
            trigger(NetMessage(self, opSrc, GetResponse(id, OpCode.NotFound, "null")) -> net);
          }
        }
        //put operation
        case Put(key, value, id) => {
          println("[KVStore] PUT operation: " + key + " - " + value);
          data += (key -> value); //update data
          //send response
          trigger(NetMessage(self, opSrc, PutResponse(id, OpCode.Ok, value)) -> net);
          pendingList.remove(id);
        }

        //cas operation
        case Cas(key, refValue, value, id) => {
          if (data.contains(key)) { //if the key exists
            if (data.get(key).get != refValue) { //not match the ref Value
              println("[KVStore] CAS operation error: " + key + " - " + refValue + " not match");
              trigger(NetMessage(self, opSrc, CasResponse(id, OpCode.NotFound, "not match")) -> net);

            } else { //success
              println("[KVStore] CAS operation: " + key + " - " + value);
              data += (key -> value);
              trigger(NetMessage(self, opSrc, CasResponse(id, OpCode.Ok, value)) -> net);
              pendingList.remove(id);
            }
          } else { //key not exist
            println("[KVStore] CAS operation error: " + key + " not found");
            trigger(NetMessage(self, opSrc, CasResponse(id, OpCode.NotFound, "null")) -> net);
          }
        }
      }
    }

  }


}
