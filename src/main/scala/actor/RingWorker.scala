package actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import message._

import scala.util.Random

object RingWorker {
  def props(): Props = Props(new RingWorker)
}

class RingWorker extends Actor with ActorLogging {
  val WantToWorkPercentage = 0.1
  var idToActor: Map[Int, ActorRef] = _
  var id: Int = _
  var lastValue = 0
  var isPingPresent = false
  var isPongPresent = false


  def next(): ActorRef =
    idToActor.filter(_._1 == (id + 1) % idToActor.size).head._2


  def wantToWorkForGivenPercentage(d: Double): Boolean = {
    Random.nextFloat() > d
  }

  def sendNext(value: Int): Unit = {
    if (isPingPresent && isPongPresent) {
      lastValue = - (Math.abs(lastValue) + 1)
      next() ! Ping(-lastValue)
      next() ! Pong(lastValue)
      (isPingPresent, isPongPresent) = (false, false)
    } else if (isPingPresent) {
      isPingPresent = false
    } else if (isPongPresent) {
      isPongPresent = false
    }
  }

  override def receive: Receive = {
    case Recognition(inputId, inputIdToActor) =>
      this.idToActor = inputIdToActor
      this.id = inputId
      sender() ! RecognitionAccept(inputId)

    case Start =>
      log.info(id + " start!")
      lastValue = -1
      idToActor.head._2 ! Ping(1)
      idToActor.head._2 ! Pong(-1)

    case Ping(value) =>
      isPingPresent = true
      if (lastValue == value) {
        //regenerate
      } else if (wantToWorkForGivenPercentage(WantToWorkPercentage)) {

      } else {
        sendNext(value)
      }


    case Pong(value) =>
      isPongPresent = true
      if (lastValue == value) {
        //regenerate
      }
  }
}
