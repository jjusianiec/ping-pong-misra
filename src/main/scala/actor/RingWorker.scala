package actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import message._

object RingWorker {
    def props(): Props = Props(new RingWorker)
}

class RingWorker extends Actor with ActorLogging{
  var idToActor: Map[Int, ActorRef] = _
  var id: Int = _
  override def receive: Receive = {
    case Recognition(inputId, inputIdToActor) =>
      this.idToActor = inputIdToActor
      this.id = inputId
      sender() ! RecognitionAccept(inputId)

    case Start =>
      log.info(id + " start!")
    case Ping(value) =>
    case Pong(value) =>
  }
}
