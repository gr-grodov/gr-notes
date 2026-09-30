package gr.grodov.gr_notes.infrastructure.bootstrap

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import gr.grodov.gr_notes.common.logs.Logging
import gr.grodov.gr_notes.infrastructure.auth.OidcDiscovery
import gr.grodov.gr_notes.infrastructure.domain.AppDatabase
import slick.jdbc.PostgresProfile.api.Database

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Application extends Logging {

    def start()(implicit system: ActorSystem[_]): Unit = {
        implicit val ec: ExecutionContext = system.executionContext

        val config = ApplicationConfig()
        logger.info("Application config loaded")

        val migrateResult = AppDatabase.migrate(config.database)
        logger.info(s"Flyway migration finished: $migrateResult")

        val database = AppDatabase.byConfig(config.database)

        val startup = for {
            oidcMetadata <- new OidcDiscovery(config.oidc).discovery()
            _ = logger.info(s"OIDC discovery completed for issuer ${oidcMetadata.issuer}")

            components = new ApplicationComponents(config, database, oidcMetadata)
            routes = ApplicationRoutes(components)

            binding <- Http().newServerAt(config.http.interface, config.http.port).bind(routes)
        } yield binding

        startup.onComplete {
            case Success(binding) =>
                onStarted(binding, database, config)
            case Failure(ex) =>
                logger.error("Application startup failed", ex)
                database.close()
                system.terminate()
        }
    }

    private def onStarted(binding: ServerBinding, database: Database, config: ApplicationConfig)(implicit system: ActorSystem[_]): Unit = {
        logger.info(s"Server started on ${config.http.interface}:${config.http.port}")
        sys.addShutdownHook {
            binding.unbind()
            database.close()
            system.terminate()
        }
    }
}