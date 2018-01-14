package actor.fsm.worker

import akka.actor.ActorRef

class RingWorkerState {
  var idToActor: Map[Int, ActorRef] = _
  var id: Int = _
  var lastValue = 0
  var criticalSectionActor: ActorRef = _
}
