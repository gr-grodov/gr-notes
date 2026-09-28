import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import common.logs.Logging
import infrastructure.bootstrap.Application

object Main extends Logging {

    def main(args: Array[String]): Unit = {
        implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "gr-notes")
        Application.start()
    }
}
