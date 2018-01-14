package actor.fsm.worker

import actor.RingWorker
import akka.actor.Actor.Receive
import message.{EnterCriticalSection, Ping, Pong, Start}

import scala.util.Random

object NoToken {
  val WantToWorkPercentage = 0.1

  def wantToEnterCriticalSection(d: Double): Boolean = {
    Random.nextFloat() < d
  }

  def handle(actor: RingWorker, state: RingWorkerState): Receive = {
    case Start =>
      actor.log.info(state.id + " start!")
      state.lastValue = -1
      state.idToActor(1) ! Ping(1)
      state.idToActor(1) ! Pong(-1)

    case Ping(value: Int) =>
      if (actor.isValueNotOld(value, state.lastValue)) {
        if(value == state.lastValue){
          actor.regenerate(value)
        } else if (wantToEnterCriticalSection(WantToWorkPercentage)) {
          actor.customLog("enter critical section")
          state.lastValue = value
          actor.context.become(HasPingToken.handle(actor, state))
          state.criticalSectionActor ! EnterCriticalSection(value, actor.context.self)
        } else {
          state.lastValue = value
          actor.next() ! Ping(value)
        }
      }

    case Pong(value: Int) =>
      if (actor.isValueNotOld(value, state.lastValue)) {
        if(value == state.lastValue) {
          actor.regenerate(value)
        } else {
          state.lastValue = value
          actor.next() ! Pong(value)
        }
      }
  }

}
