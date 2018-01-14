package actor.fsm.worker

import actor.RingWorker
import akka.actor.Actor.Receive
import message.{ExitCriticalSection, Ping, Pong}

object HasBothTokens {

  def handle(actor: RingWorker, state: RingWorkerState): Receive = {
    case Ping => actor.customLog("Receive ping in bothTokenState")
    case Pong => actor.customLog("Receive pong in bothTokenState")
    case ExitCriticalSection(value: Int) =>
      actor.customLog("exit critical section")
      val newValue = Math.abs(state.lastValue) + 1
      actor.next() ! Ping(newValue)
      actor.next() ! Pong(-newValue)
      state.lastValue = -newValue
      actor.context.become(NoToken.handle(actor, state))

  }

}
