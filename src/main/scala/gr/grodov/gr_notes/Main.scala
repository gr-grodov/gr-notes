package gr.grodov.gr_notes

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import gr.grodov.gr_notes.common.logs.Logging
import gr.grodov.gr_notes.infrastructure.bootstrap.Application

object Main extends Logging {

    def main(args: Array[String]): Unit = {
        implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "gr-notes")
        Application.start()
    }
}
