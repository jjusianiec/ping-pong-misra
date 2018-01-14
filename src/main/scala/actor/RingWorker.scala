package actor

import actor.fsm.worker.{NoToken, RingWorkerState}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import message._

object RingWorker {
  def props(): Props = Props(new RingWorker)
}

class RingWorker extends Actor with ActorLogging {
  var state: RingWorkerState = new RingWorkerState

  def next(): ActorRef =
    state.idToActor.filter(_._1 == (state.id + 1) % state.idToActor.size).head._2

  def isValueNotOld(value: Int, lastValue: Int): Boolean = {
    Math.abs(value) >= Math.abs(lastValue)
  }

  def regenerate(value: Int): Unit = {
    state.lastValue = -Math.abs(value)
    customLog("regenerate value " + value)
    next() ! Ping(Math.abs(value))
    next() ! Pong(-Math.abs(value))
  }

  def customLog(message: String): Unit = {
    log.info(state.id + " [" + state.lastValue + "]: " + message)
  }

  override def receive: Receive = {
    case Recognition(id, idToActor) =>
      state.idToActor = idToActor
      state.id = id
      state.criticalSectionActor = context.actorOf(CriticalSectionActor.props())
      context.become(NoToken.handle(this, state))
      sender() ! RecognitionAccept(id)
  }
}
