package gr.grodov.gr_notes.infrastructure.bootstrap

import akka.actor.typed.ActorSystem
import auth.actor.SessionActor
import gr.grodov.gr_notes.auth.actor.{OidcLoginTransactionActor, SessionActor}
import gr.grodov.gr_notes.auth.http.{AuthDirectives, AuthRoutes}
import gr.grodov.gr_notes.auth.service.{AuthService, OidcClientService, SessionService}
import gr.grodov.gr_notes.auth.utils.IdTokenValidator
import gr.grodov.gr_notes.infrastructure.auth.OidcMetadata
import gr.grodov.gr_notes.notes.domain.repo.DBNotesRepository
import gr.grodov.gr_notes.notes.http.NotesRoutes
import gr.grodov.gr_notes.notes.service.NotesService
import slick.jdbc.PostgresProfile.api.Database

import scala.concurrent.ExecutionContext

final class ApplicationComponents(
    config: ApplicationConfig,
    database: Database,
    oidcMetadata: OidcMetadata
) (implicit system: ActorSystem[_], ec: ExecutionContext) {

    // Authentication
    private val oidcLoginTransactionActor = system.systemActorOf(
        OidcLoginTransactionActor(),
        "oidc-login-transactions"
    )
    private val oidcClientService = new OidcClientService(config.oidc, oidcMetadata)
    private val sessionActor = system.systemActorOf(
        SessionActor(oidcClientService),
        "sessions"
    )
    private val sessionService = new SessionService(sessionActor)
    private val idTokenValidator = new IdTokenValidator(config.oidc, oidcMetadata)
    private val authService = new AuthService(
        oidcClientService,
        sessionService,
        idTokenValidator,
        oidcLoginTransactionActor
    )
    val authDirectives = new AuthDirectives(sessionService)
    val authRoutes = new AuthRoutes(authService)

    // Notes API
    private val notesRepository = new DBNotesRepository(database)
    private val notesService = new NotesService(notesRepository)
    val notesRoutes = new NotesRoutes(notesService, authDirectives)
}