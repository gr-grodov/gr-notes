package auth.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import gr.grodov.gr_notes.auth.service.{OidcClientService, TokensInfo}

import java.time.Instant
import java.util.UUID
import scala.util.{Failure, Success}

object SessionActor {

    sealed trait Command

    final case class CreateSession(
        subject: String,
        oidcSid: String,
        accessToken: String,
        refreshToken: Option[String],
        accessTokenExpiresIn: Long,
        replyTo: ActorRef[CreateSessionResult]
    ) extends Command
    final case class GetSession(
        sessionId: String,
        replyTo: ActorRef[GetSessionResult]
    ) extends Command
    final case class UpdateTokens(
        sessionId: String,
        accessToken: String,
        refreshToken: Option[String],
        accessTokenExpiresIn: Long,
        replyTo: ActorRef[UpdateTokensResult]
    ) extends Command
    final case class DeleteSession(
        sessionId: String,
        replyTo: ActorRef[DeleteSessionResult]
    ) extends Command

    final case class DeleteSessionBySid(
        oidcSid: String,
        replyTo: ActorRef[DeleteSessionBySidResult]
    ) extends Command

    private final case class TokensRefreshed(sessionId: String, tokens: TokensInfo) extends Command

    private final case class TokenRefreshFailed(sessionId: String, error: Throwable) extends Command


    sealed trait CreateSessionResult
    final case class SessionCreated(sessionId: String) extends CreateSessionResult

    sealed trait GetSessionResult
    final case class SessionFound(session: UserSession) extends GetSessionResult
    case object SessionNotFound extends GetSessionResult

    sealed trait UpdateTokensResult
    case object TokensUpdated extends UpdateTokensResult
    case object SessionNotFoundForUpdate extends UpdateTokensResult

    sealed trait DeleteSessionResult
    case object SessionDeleted extends DeleteSessionResult

    sealed trait DeleteSessionBySidResult
    final case class SessionDeletedBySid(count: Int) extends DeleteSessionBySidResult


    private final case class State(
        sessions: Map[String, UserSession],
        sessionsByOidcSid: Map[String, Set[String]],
        pendingRefreshes: Map[String, List[ActorRef[GetSessionResult]]] = Map.empty
    )

    def apply(oidcClientService: OidcClientService): Behavior[Command] =
        active(oidcClientService, State(sessions = Map.empty, sessionsByOidcSid = Map.empty))

    private def active(oidcClientService: OidcClientService, state: State): Behavior[Command] = Behaviors.receive {
        (context, message) => message match {
            case CreateSession(subject, oidcSid, accessToken, refreshToken, accessTokenExpiresIn, replyTo) =>
                val sessionId = UUID.randomUUID().toString
                val now = Instant.now()
                val session = UserSession(
                    sessionId = sessionId,
                    subject = subject,
                    oidcSid = oidcSid,
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    accessTokenExpiresAt = now.plusSeconds(accessTokenExpiresIn),
                    createdAt = now
                )

                val updatedSessions = state.sessions.updated(sessionId, session)
                val sessionIds = state.sessionsByOidcSid.getOrElse(oidcSid, Set.empty).incl(sessionId)
                val updatedSessionsByOidcSid = state.sessionsByOidcSid.updated(oidcSid, sessionIds)

                replyTo ! SessionCreated(sessionId)
                active(oidcClientService, state.copy(sessions = updatedSessions, sessionsByOidcSid = updatedSessionsByOidcSid))

            case GetSession(sessionId, replyTo) =>
                state.sessions.get(sessionId) match {
                    case None =>
                        replyTo ! SessionNotFound
                        Behaviors.same

                    case Some(session) if !isExpired(session) =>
                        replyTo ! SessionFound(session)
                        Behaviors.same

                    case Some(_) if state.pendingRefreshes.contains(sessionId) =>
                        val waiters = replyTo :: state.pendingRefreshes(sessionId)
                        active(oidcClientService, state.copy(pendingRefreshes = state.pendingRefreshes.updated(sessionId, waiters)))

                    case Some(session) =>
                        session.refreshToken match {
                            case Some(refreshToken) =>
                                context.pipeToSelf(oidcClientService.refresh(refreshToken)) {
                                    case Success(tokens) => TokensRefreshed(sessionId, tokens)
                                    case Failure(ex) => TokenRefreshFailed(sessionId, ex)
                                }
                                active(oidcClientService, state.copy(pendingRefreshes = state.pendingRefreshes.updated(sessionId, List(replyTo))))

                            case None =>
                                replyTo ! SessionNotFound
                                Behaviors.same
                        }
                }

            case TokensRefreshed(sessionId, tokens) =>
                val waiters = state.pendingRefreshes.getOrElse(sessionId, Nil)

                state.sessions.get(sessionId) match {
                    case Some(session) =>
                        val updated = session.copy(
                            accessToken = tokens.accessToken,
                            refreshToken = tokens.refreshToken.orElse(session.refreshToken),
                            accessTokenExpiresAt = Instant.now().plusSeconds(tokens.expireIn)
                        )
                        waiters.foreach(_ ! SessionFound(updated))
                        active(oidcClientService, state.copy(
                            sessions = state.sessions.updated(sessionId, updated),
                            pendingRefreshes = state.pendingRefreshes - sessionId
                        ))

                    case None =>
                        waiters.foreach(_ ! SessionNotFound)
                        active(oidcClientService, state.copy(pendingRefreshes = state.pendingRefreshes - sessionId))
                }

            case TokenRefreshFailed(sessionId, error) =>
                context.log.warn(s"Не удалось обновить токены для сессии $sessionId", error)
                val waiters = state.pendingRefreshes.getOrElse(sessionId, Nil)
                waiters.foreach(_ ! SessionNotFound)

                val updatedState = state.sessions.get(sessionId) match {
                    case Some(session) =>
                        val remainingIds = state.sessionsByOidcSid.getOrElse(session.oidcSid, Set.empty) - sessionId
                        val updatedByOidcSid =
                            if (remainingIds.isEmpty) state.sessionsByOidcSid - session.oidcSid
                            else state.sessionsByOidcSid.updated(session.oidcSid, remainingIds)

                        state.copy(
                            sessions = state.sessions - sessionId,
                            sessionsByOidcSid = updatedByOidcSid,
                            pendingRefreshes = state.pendingRefreshes - sessionId
                        )
                    case None =>
                        state.copy(pendingRefreshes = state.pendingRefreshes - sessionId)
                }
                active(oidcClientService, updatedState)

            case UpdateTokens(sessionId, accessToken, refreshToken, accessTokenExpiresIn, replyTo) =>
                state.sessions.get(sessionId) match {
                    case Some(session) =>
                        val updatedSession = session.copy(
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            accessTokenExpiresAt = Instant.now().plusSeconds(accessTokenExpiresIn)
                        )

                        replyTo ! TokensUpdated
                        active(oidcClientService, state.copy(sessions = state.sessions.updated(sessionId, updatedSession)))

                    case None =>
                        replyTo ! SessionNotFoundForUpdate
                        Behaviors.same
                }

            case DeleteSession(sessionId, replyTo) =>
                state.sessions.get(sessionId) match {
                    case Some(session) =>
                        val updatedSessions = state.sessions - sessionId

                        val updatedSessionIds = state.sessionsByOidcSid.getOrElse(session.oidcSid, Set.empty) - sessionId
                        val updatedSessionsByOidcSid = if (updatedSessionIds.isEmpty) {
                            state.sessionsByOidcSid - session.oidcSid
                        } else {
                            state.sessionsByOidcSid.updated(session.oidcSid, updatedSessionIds)
                        }

                        replyTo ! SessionDeleted
                        active(oidcClientService, state.copy(sessions = updatedSessions, sessionsByOidcSid = updatedSessionsByOidcSid))

                    case None =>
                        replyTo ! SessionDeleted
                        Behaviors.same
                }

            case DeleteSessionBySid(oidcSid, replyTo) =>
                val sessionIds = state.sessionsByOidcSid.getOrElse(oidcSid, Set.empty)

                val updatedSessions = state.sessions -- sessionIds
                val updatedSessionsByOidcSid = state.sessionsByOidcSid - oidcSid

                replyTo ! SessionDeletedBySid(sessionIds.size)

                active(oidcClientService, state.copy(sessions = updatedSessions, sessionsByOidcSid = updatedSessionsByOidcSid))
        }
    }

    private def isExpired(session: UserSession): Boolean =
        !Instant.now().isBefore(session.accessTokenExpiresAt)
}


final case class UserSession(
    sessionId: String,
    subject: String,
    oidcSid: String,
    accessToken: String,
    refreshToken: Option[String],
    accessTokenExpiresAt: Instant,
    createdAt: Instant
)