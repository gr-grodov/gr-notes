package auth.actor

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import java.time.Instant
import java.util.UUID

object SessionActor {

    sealed trait Command

    final case class CreateSession(
        subject: String,
        oidcSid: String,
        accessToken: String,
        refreshToken: Option[String],
        accessTokenExpiresAt: Long,
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
        accessTokenExpiresAt: Long,
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


    sealed trait CreateSessionResult
    final case class SessionCreated(sessionId: String) extends CreateSessionResult

    sealed trait GetSessionResult
    final case class SessionFound(session: OidcSession) extends GetSessionResult
    case object SessionNotFound extends GetSessionResult


    sealed trait UpdateTokensResult
    case object TokensUpdated extends UpdateTokensResult
    case object SessionNotFoundForUpdate extends UpdateTokensResult


    sealed trait DeleteSessionResult
    case object SessionDeleted extends DeleteSessionResult


    sealed trait DeleteSessionBySidResult
    final case class SessionDeletedBySid(count: Int) extends DeleteSessionBySidResult


    private final case class State(
        sessions: Map[String, OidcSession],
        sessionsByOidcSid: Map[String, Set[String]]
    )

    def apply(): Behavior[Command] = active(State(sessions = Map.empty, sessionsByOidcSid = Map.empty))

    private def active(state: State): Behavior[Command] = Behaviors.receiveMessage {

        case CreateSession(subject, oidcSid, accessToken, refreshToken, accessTokenExpiresAt, replyTo) =>
            val sessionId = UUID.randomUUID().toString
            val now = Instant.now()
            val session = OidcSession(
                subject = subject,
                oidcSid = oidcSid,
                accessToken = accessToken,
                refreshToken = refreshToken,
                accessTokenExpiresAt = now.plusSeconds(accessTokenExpiresAt),
                createdAt = now
            )

            val updatedSessions = state.sessions.updated(sessionId, session)

            val sessionIds = state.sessionsByOidcSid.getOrElse(oidcSid, Set.empty).incl(sessionId)
            val updatedSessionsByOidcSid = state.sessionsByOidcSid.updated(oidcSid, sessionIds)

            replyTo ! SessionCreated(sessionId)
            active(state.copy(sessions = updatedSessions, sessionsByOidcSid = updatedSessionsByOidcSid))

        case GetSession(sessionId, replyTo) =>
            state.sessions.get(sessionId) match {
                case Some(session) => replyTo ! SessionFound(session)
                case None => replyTo ! SessionNotFound
            }
            Behaviors.same


        case UpdateTokens(sessionId, accessToken, refreshToken, accessTokenExpiresAt, replyTo) =>
            state.sessions.get(sessionId) match {
                case Some(session) =>
                    val updatedSession = session.copy(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        accessTokenExpiresAt = Instant.now().plusSeconds(accessTokenExpiresAt)
                    )

                    replyTo ! TokensUpdated
                    active(state.copy(sessions = state.sessions.updated(sessionId, updatedSession)))

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
                    active(state.copy(sessions = updatedSessions, sessionsByOidcSid = updatedSessionsByOidcSid))

                case None =>
                    replyTo ! SessionDeleted
                    Behaviors.same
            }


        case DeleteSessionBySid(oidcSid, replyTo) =>
            val sessionIds = state.sessionsByOidcSid.getOrElse(oidcSid, Set.empty)

            val updatedSessions = state.sessions -- sessionIds
            val updatedSessionsByOidcSid = state.sessionsByOidcSid - oidcSid

            replyTo ! SessionDeletedBySid(sessionIds.size)

            active(state.copy(sessions = updatedSessions, sessionsByOidcSid = updatedSessionsByOidcSid))
    }
}


final case class OidcSession(
    subject: String,
    oidcSid: String,
    accessToken: String,
    refreshToken: Option[String],
    accessTokenExpiresAt: Instant,
    createdAt: Instant
)