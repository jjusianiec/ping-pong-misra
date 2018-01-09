package actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import message.{Recognition, RecognitionAccept, Start}

object RingMaster {
  def props(): Props = Props(new RingMaster)
}

class RingMaster extends Actor with ActorLogging {
  val NumberOfNodes: Int = 10
  private val idToActor: Map[Int, ActorRef] = List.range(0, NumberOfNodes)
    .map(number => number -> context.actorOf(RingWorker.props())).toMap
  var acceptCounter: Int = NumberOfNodes


  override def preStart(): Unit = log.info("Ring master started")

  def startExecutionWhenAllWorkerAccepted(id: Int): Unit = {
    acceptCounter -= 1
    if(acceptCounter == 0){
      idToActor.head._2 ! Start
    }
  }

  override def receive: Receive = {
    case RecognitionAccept(id) => startExecutionWhenAllWorkerAccepted(id);
  }

  def init(): Unit = {
    idToActor.foreach(tuple => tuple._2 ! Recognition(tuple._1, idToActor))
  }

  init()
}
