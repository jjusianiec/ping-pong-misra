package actor

import akka.actor.{Actor, ActorLogging, Props}
import message.EnterCriticalSection

object CriticalSectionActor {
  def props(): Props = Props(new CriticalSectionActor)
}

class CriticalSectionActor extends Actor with ActorLogging{
  val Second = 1000
  val CriticalSectionWorkTime: Int = 5 * Second

  def processCriticalSection(): Unit = {
    Thread.sleep(CriticalSectionWorkTime)
  }

  override def receive: Receive = {
    case EnterCriticalSection =>
  }
}
