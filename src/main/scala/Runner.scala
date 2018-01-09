import actor.RingMaster
import akka.actor.ActorSystem

import scala.io.StdIn

object Runner {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("ring-poc")

    try {
      val ringMaster = system.actorOf(RingMaster.props(), "ring-master")
      StdIn.readLine()
    } finally {
      system.terminate()
    }
  }

}
