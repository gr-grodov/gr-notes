package auth.http

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import auth.service.{SessionService, UserInfo}

import scala.util.{Failure, Success}

final class AuthDirectives(sessionService: SessionService) {

    def authenticated: Directive1[UserInfo] = optionalCookie(AuthRoutes.COOKIE_SESSION_ID).flatMap {
        case None => reject(MissingOrInvalidSession)
        case Some(cookie) => onComplete(sessionService.getUserBySessionId(cookie.value)).flatMap {
            case Success(Some(userSub)) => provide(userSub)
            case Success(None) => reject(MissingOrInvalidSession)
            case Failure(_) => reject(MissingOrInvalidSession)
        }
    }
}

case object MissingOrInvalidSession extends akka.http.scaladsl.server.Rejection