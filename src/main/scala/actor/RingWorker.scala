package actor

import actor.fsm.worker.{NoToken, RingWorkerState}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import message._

import scala.util.Random

object RingWorker {
  def props(): Props = Props(new RingWorker)
}

class RingWorker extends Actor with ActorLogging {
  val WantToWorkPercentage = 0.1
  var state: RingWorkerState = new RingWorkerState
//  var idToActor: Map[Int, ActorRef] = _
//  var id: Int = _
//  var lastValue = 0
//  var criticalSectionActor: ActorRef = context.actorOf(CriticalSectionActor.props())
  var isPingPresent = false
  var isPongPresent = false


  def next(): ActorRef =
    state.idToActor.filter(_._1 == (state.id + 1) % state.idToActor.size).head._2

  def wantToEnterCriticalSection(d: Double): Boolean = {
    Random.nextFloat() < d
  }

  def sendNext(value: Int): Unit = {
    if (isPingPresent && isPongPresent) {
      incarnate(value)
    } else if (isPingPresent) {
      isPingPresent = false
      state.lastValue = Math.abs(value)
      customLog("send ping")
      next() ! Ping(Math.abs(value))
    } else if (isPongPresent) {
      isPongPresent = false
      state.lastValue = -Math.abs(value)
      customLog("send pong to")
      next() ! Pong(-Math.abs(value))
    }
  }

  def isValueNotOld(value: Int): Boolean = {
    Math.abs(value) >= Math.abs(state.lastValue)
  }

  def regenerate(value: Int): Unit = {
    state.lastValue = -Math.abs(value)
    isPongPresent = false
    isPingPresent = false
    customLog("regenerate value " + value)
    next() ! Ping(Math.abs(value))
    next() ! Pong(-Math.abs(value))
  }

  def incarnate(value: Int): Unit = {
    isPingPresent = false
    isPongPresent = false
    state.lastValue = -(Math.abs(value) + 1)
    customLog("incarnate to value " + (-(Math.abs(value) + 1)) + " value: " + value)
    next() ! Ping(-state.lastValue)
    next() ! Pong(state.lastValue)
  }

  def customLog(message: String): Unit = {
    log.info(state.id + " [" + state.lastValue + "]: " + message)
  }

  override def receive: Receive = {
    case Recognition(inputId, inputIdToActor) =>
      state.idToActor = inputIdToActor
      state.id = inputId
      state.criticalSectionActor = context.actorOf(CriticalSectionActor.props())
      sender() ! RecognitionAccept(inputId)

    case Start =>
      log.info(state.id + " start!")
      state.lastValue = -1
      state.idToActor(1) ! Ping(1)
      state.idToActor(1) ! Pong(-1)
      context.become(NoToken.handle(state))

//    case Ping(value) =>
//      if (isValueNotOld(value)) {
//        isPingPresent = true
//        if (state.lastValue == value) {
//          regenerate(value)
//        } else if (wantToEnterCriticalSection(WantToWorkPercentage)) {
//          customLog("enter critical section")
//          state.criticalSectionActor ! EnterCriticalSection
//        } else {
//          customLog("Im lazy!")
//          sendNext(value)
//        }
//      }
//
//    case Pong(value) =>
//      if (isValueNotOld(value)) {
//        isPongPresent = true
//        if (state.lastValue == value) {
//          regenerate(value)
//        } else {
//          sendNext(value)
//        }
//      }
//
//    case ExitCriticalSection => {
//      customLog("exit critical section")
//    }
  }
}
