package auth.service

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import auth.actor.SessionActor
import auth.actor.SessionActor.{CreateSession, CreateSessionResult, GetSession}
import cats.data.OptionT

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

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
                accessTokenExpiresAt = tokens.tokens.expireIn,
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