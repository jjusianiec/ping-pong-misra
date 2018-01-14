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
  var criticalSectionActor: ActorRef = context.actorOf(CriticalSectionActor.props())


  def next(): ActorRef =
    idToActor.filter(_._1 == (id + 1) % idToActor.size).head._2

  def wantToEnterCriticalSection(d: Double): Boolean = {
    Random.nextFloat() < d
  }

  def sendNext(value: Int): Unit = {
    if (isPingPresent && isPongPresent) {
      incarnate(value)
    } else if (isPingPresent) {
      isPingPresent = false
      lastValue = Math.abs(value)
      customLog("send ping")
      next() ! Ping(Math.abs(value))
    } else if (isPongPresent) {
      isPongPresent = false
      lastValue = -Math.abs(value)
      customLog("send pong to")
      next() ! Pong(-Math.abs(value))
    }
  }

  def isValueNotOld(value: Int): Boolean = {
    Math.abs(value) >= Math.abs(lastValue)
  }

  def regenerate(value: Int): Unit = {
    lastValue = -Math.abs(value)
    isPongPresent = false
    isPingPresent = false
    customLog("regenerate value " + value)
    next() ! Ping(Math.abs(value))
    next() ! Pong(-Math.abs(value))
  }

  def incarnate(value: Int): Unit = {
    isPingPresent = false
    isPongPresent = false
    lastValue = -(Math.abs(value) + 1)
    customLog("incarnate to value " + (-(Math.abs(value) + 1)) + " value: " + value)
    next() ! Ping(-lastValue)
    next() ! Pong(lastValue)
  }

  def customLog(message: String): Unit = {
    log.info(id + " [" + lastValue + "]: " + message)
  }

  override def receive: Receive = {
    case Recognition(inputId, inputIdToActor) =>
      this.idToActor = inputIdToActor
      this.id = inputId
      sender() ! RecognitionAccept(inputId)

    case Start =>
      log.info(id + " start!")
      lastValue = -1
      idToActor(1) ! Ping(1)
      idToActor(1) ! Pong(-1)

    case Ping(value) =>
      if (isValueNotOld(value)) {
        isPingPresent = true
        if (lastValue == value) {
          regenerate(value)
        } else if (wantToEnterCriticalSection(WantToWorkPercentage)) {
          customLog("enter critical section")
          criticalSectionActor ! EnterCriticalSection
        } else {
          customLog("Im lazy!")
          sendNext(value)
        }
      }

    case Pong(value) =>
      if (isValueNotOld(value)) {
        isPongPresent = true
        if (lastValue == value) {
          regenerate(value)
        } else {
          sendNext(value)
        }
      }

    case ExitCriticalSection => {
      customLog("exit critical section")
    }
  }
}
