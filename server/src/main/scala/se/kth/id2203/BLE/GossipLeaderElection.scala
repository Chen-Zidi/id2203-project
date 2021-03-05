package se.kth.id2203.BLE;

import se.kth.id2203.networking.{NetAddress, NetMessage}
import se.sics.kompics.network.Network
import se.sics.kompics.sl._
import se.sics.kompics.timer.{ScheduleTimeout, Timeout, Timer}
import se.sics.kompics.{KompicsEvent, Start}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer;

class BallotLeaderElection extends Port{
  indication[BLE_Leader];
  request[BLE_Start];
}

case class BLE_Start(pi: Set[NetAddress]) extends KompicsEvent;

case class BLE_Leader(address: NetAddress, l: Long) extends KompicsEvent;

case class CheckTimeout(timeout: ScheduleTimeout) extends Timeout(timeout);

case class HeartbeatReq(round: Long, highestBallot: Long) extends KompicsEvent;

case class HeartbeatResp(round: Long, ballot: Long) extends KompicsEvent;



//class GossipLeaderElection(init: Init[GossipLeaderElection]) extends ComponentDefinition {
class GossipLeaderElection extends ComponentDefinition {


  val ble = provides[BallotLeaderElection];
  val pl = requires[Network];
  val timer = requires[Timer];

  var topology: Set[NetAddress] = Set();
  val self = cfg.getValue[NetAddress]("id2203.project.address");

//  val (self, topology) = init match{
//    case Init(self:NetAddress, topology: Set[NetAddress]) => (self, topology)
//  }
  //val self = cfg.getValue[NetAddress]("id2203.project.Address");
  //val topology = ListBuffer.empty[NetAddress];

  //what is delta here
  //val delta = cfg.getValue[Long]("ble.simulation.delay");
  val delta = 50;
  var majority =  0;

  //what is period here?
  //private var period = cfg.getValue[Long]("ble.simulation.delay");
  private var period = 1500l;
  private val ballots = mutable.Map.empty[NetAddress, Long];

  private var round = 0l;
  private var ballot = ballotFromNAddress(0, self);

  private var leader: Option[(Long, NetAddress)] = None;
  private var highestBallot: Long = ballot;

  private val ballotOne = 0x0100000000l;

  def ballotFromNAddress(n: Int, adr: NetAddress): Long = {
    val nBytes = com.google.common.primitives.Ints.toByteArray(n);
    val addrBytes = com.google.common.primitives.Ints.toByteArray(adr.hashCode());
    val bytes = nBytes ++ addrBytes;
    val r = com.google.common.primitives.Longs.fromByteArray(bytes);
    assert(r > 0); // should not produce negative numbers!
    r
  }

  def incrementBallotBy(ballot: Long, inc: Int): Long = {
    ballot + inc.toLong * ballotOne
  }

  private def incrementBallot(ballot: Long): Long = {
    ballot + ballotOne
  }

  private def startTimer(delay: Long): Unit = {
    val scheduledTimeout = new ScheduleTimeout(period);
    scheduledTimeout.setTimeoutEvent(CheckTimeout(scheduledTimeout));
    trigger(scheduledTimeout -> timer);
  }

  private def makeLeader(topProcess: (Long, NetAddress)) {
    /* INSERT YOUR CODE HERE */
    leader = Some(topProcess);
  }

  private def checkLeader() {
    /* INSERT YOUR CODE HERE */
    var topProcess = self;
    var topBallot = ballot;


    //   val temp = (self, ballot);
    ballots += (self->ballot);
    //get top ballot
    for(b <- ballots){
      if(b._2 > topBallot){
        topBallot = b._2;
        topProcess = b._1;
      }
    }
    var top = (topBallot, topProcess);
    if(topBallot < highestBallot){
      while(ballot <= highestBallot){
        ballot = incrementBallotBy(ballot, 1);
      }
      leader = None;
    }else{
      if(Some(top) != leader ){
        highestBallot = topBallot;
        makeLeader((topBallot, topProcess));
        trigger(BLE_Leader(topProcess, topBallot)->ble);
      }
    }

  }

  ble uponEvent {
    case BLE_Start(pi) =>  {
      topology = pi;
      majority = (topology.size / 2) + 1;
      startTimer(period);
    }
  }

  timer uponEvent {
    case CheckTimeout(_) => {
      /* INSERT YOUR CODE HERE */
      if(ballots.size + 1 >= majority){
        checkLeader();
      }
      ballots.clear;
      round = round + 1;
      for(p <- topology){
        if(p != self){
          trigger(NetMessage(self, p, HeartbeatReq(round, highestBallot))->pl);
        }
      }
      startTimer(period);
    }
  }

  pl uponEvent {
    case NetMessage(header, HeartbeatReq(r, hb)) => {
      /* INSERT YOUR CODE HERE */
      if(hb > highestBallot){
        highestBallot = hb;
      }
      trigger(NetMessage(self, header.src, HeartbeatResp(r,ballot))->pl);
    }
    case NetMessage(header, HeartbeatResp(r, b)) => {
      /* INSERT YOUR CODE HERE */
      if(r == round){
        ballots += (header.src -> b);
      }else{
        period = period + delta;
      }
    }
  }
}