package actor.fsm.worker

import actor.RingWorker
import akka.actor.Actor.Receive
import message.{ExitCriticalSection, Ping, Pong}

object HasPingToken {

  def handle(actor: RingWorker, state: RingWorkerState): Receive = {
    case Ping => actor.customLog("Receive ping in hasPingToken")
    case Pong(value: Int) =>
      if (actor.isValueNotOld(value, state.lastValue)) {
        state.lastValue = value
        actor.context.become(HasBothTokens.handle(actor, state))
      }

    case ExitCriticalSection(value) =>
      if (state.lastValue != value) {
        actor.customLog("value changed since entered to critical section: " + value + " lastValue: " + state.lastValue) //it should not happen
        throw new RuntimeException()
      } else {
        actor.next() ! Ping(value)
        actor.context.become(NoToken.handle(actor, state))
      }
  }

}
