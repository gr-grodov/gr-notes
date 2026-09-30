package infrastructure.bootstrap

import akka.actor.typed.ActorSystem
import auth.actor.{OidcLoginTransactionActor, SessionActor}
import auth.http.{AuthDirectives, AuthRoutes}
import auth.service.{AuthService, OidcClientService, SessionService}
import auth.utils.IdTokenValidator
import infrastructure.AppConfig
import infrastructure.auth.OidcMetadata
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

    // Authentication
    private val oidcLoginTransactionActor = system.systemActorOf(
        OidcLoginTransactionActor(),
        "oidc-login-transactions"
    )
    private val sessionActor = system.systemActorOf(
        SessionActor(),
        "sessions"
    )
    private val sessionService = new SessionService(sessionActor)
    private val oidcClientService = new OidcClientService(config.oidc, oidcMetadata)
    private val idTokenValidator = new IdTokenValidator(config.oidc, oidcMetadata)
    private val authService = new AuthService(
        oidcClientService,
        sessionService,
        idTokenValidator,
        oidcLoginTransactionActor
    )
    val authDirectives = new AuthDirectives(sessionService)
    val authRoutes = new AuthRoutes(authService, sessionService)

    // Notes API
    private val notesRepository = new DBNotesRepository(database)
    private val notesService = new NotesService(notesRepository)
    val notesRoutes = new NotesRoutes(notesService, authDirectives)
}