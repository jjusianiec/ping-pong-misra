package actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import message.{EnterCriticalSection, ExitCriticalSection}

object CriticalSectionActor {
  def props(): Props = Props(new CriticalSectionActor)
}

class CriticalSectionActor extends Actor with ActorLogging {

  def processCriticalSection(): Unit = {
    Thread.sleep(5000)
  }

  override def receive: Receive = {
    case EnterCriticalSection(value: Int, response: ActorRef) =>
      processCriticalSection()
      response ! ExitCriticalSection(value)
  }
}
