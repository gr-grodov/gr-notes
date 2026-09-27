import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.concat
import common.logs.Logging
import infrastructure.AppConfig
import infrastructure.domain.AppDatabase
import notes.http.NotesRoutes

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main extends Logging {

    def main(args: Array[String]): Unit = {
        val config = AppConfig()
        logger.info("Application config loaded")

        val migrateResult = AppDatabase.migrate(config.database)
        logger.info(s"Flyway migration finished: $migrateResult")

        val database = AppDatabase.byConfig(config.database)

        implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "gr-notes")
        implicit val execContext: ExecutionContextExecutor = system.executionContext

        val routes: Route = concat(NotesRoutes.routes)
        val bindingFuture = Http().newServerAt(config.http.interface, config.http.port).bind(routes)
        bindingFuture.onComplete {
            case Success(binding) =>
                logger.info(s"Server start on port ${config.http.port}")
                sys.addShutdownHook {
                    binding.unbind()
                    database.close()
                    system.terminate()
                }
            case Failure(ex) =>
                logger.error("Server failed started", ex)
                system.terminate()
        }
    }
}
