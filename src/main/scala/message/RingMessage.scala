package message

import akka.actor.ActorRef

sealed trait RingMessage
case class Ping(value: Int) extends RingMessage
case class Pong(value: Int) extends RingMessage
case class Recognition(id: Int, idToActor: Map[Int, ActorRef]) extends RingMessage
case class RecognitionAccept(id: Int) extends RingMessage
case object Start extends RingMessage
