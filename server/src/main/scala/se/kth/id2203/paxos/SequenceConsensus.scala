package se.kth.id2203.paxos;



import se.kth.id2203.kvstore.Operation
import se.kth.id2203.networking.NetAddress
import se.sics.kompics.sl._
import se.sics.kompics.KompicsEvent





class SequenceConsensus extends Port {
  request[SC_Propose];
  request[InitializeTopology];
  indication[SC_Decide];
}

case class SC_Propose(value: Operation) extends KompicsEvent;
case class SC_Decide(value: Operation) extends KompicsEvent;
case class InitializeTopology(topology: Set[NetAddress]) extends KompicsEvent;




