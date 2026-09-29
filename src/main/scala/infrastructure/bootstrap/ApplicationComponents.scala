package infrastructure.bootstrap

import akka.actor.typed.ActorSystem
import auth.service.OidcClientService
import infrastructure.AppConfig
import infrastructure.auth.{OidcDiscovery, OidcMetadata}
import notes.domain.repo.DBNotesRepository
import notes.http.NotesRoutes
import notes.service.NotesService
import slick.jdbc.PostgresProfile.api.Database

import scala.concurrent.ExecutionContext

final class ApplicationComponents(
    config: AppConfig,
    database: Database,
    oidcMetadata: OidcMetadata
) (implicit system: ActorSystem[_], ec: ExecutionContext) {

    private val notesRepository = new DBNotesRepository(database)
    private val notesService = new NotesService(notesRepository)
    val notesRoutes = new NotesRoutes(notesService)

    val oidcClientService = new OidcClientService(config.oidc, oidcMetadata)
}