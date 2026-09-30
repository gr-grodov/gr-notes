package gr.grodov.gr_notes.auth.service

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import gr.grodov.gr_notes.auth.actor.SessionActor.{CreateSession, GetSession}
import gr.grodov.gr_notes.auth.actor.SessionActor

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

final class SessionService(
    sessionActor: ActorRef[SessionActor.Command]
) (implicit system: ActorSystem[_], ec: ExecutionContext) {

    private implicit val askTimeout: Timeout = 3.seconds

    def createSession(tokens: AuthenticatedTokens): Future[SessionInfo] = {
        sessionActor.ask[SessionActor.CreateSessionResult] { replyTo =>
            CreateSession(
                subject = tokens.identityUser.subject,
                oidcSid = tokens.identityUser.sid,
                accessToken = tokens.tokens.accessToken,
                refreshToken = tokens.tokens.refreshToken,
                accessTokenExpiresIn = tokens.tokens.expireIn,
                replyTo = replyTo
            )
        }.map {
            case SessionActor.SessionCreated(sessionId) => SessionInfo(sessionId)
        }
    }

    def getUserBySessionId(sessionId: String): Future[Option[UserInfo]] = {
        sessionActor.ask[SessionActor.GetSessionResult] { replyTo =>
            GetSession(sessionId, replyTo)
        }.map {
            case SessionActor.SessionFound(session) => Some(UserInfo(session.subject))
            case SessionActor.SessionNotFound => None
        }
    }
}

final case class SessionInfo(sessionId: String)
final case class UserInfo(
    userSub: String
)