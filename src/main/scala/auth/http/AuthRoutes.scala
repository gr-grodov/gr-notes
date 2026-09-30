package auth.http

import akka.http.javadsl.model.headers
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{HttpCookie, SameSite}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import auth.http.AuthRoutes.COOKIE_SESSION_ID
import auth.service.{AuthService, SessionInfo, SessionService}

import scala.util.Success

final class AuthRoutes(authService: AuthService, sessionService: SessionService) {
    val routes: Route = pathPrefix("api" / "auth") {
        concat(
            path("login") {
                get {
                    login()
                }
            },
            path("callback") {
                get {
                    callback()
                }
            }
        )
    }

    private def login(): Route = {
        onSuccess(authService.ssoLoginURI) { uri =>
            redirect(uri, StatusCodes.Found)
        }
    }

    private def callback(): Route = {
        parameters("code", "state") { (code, state) =>
            onSuccess(authService.authenticate(code, state)) { sessionInfo =>
                setCookie(sessionCookie(sessionInfo)) {
                    complete(StatusCodes.OK)
                }
            }
        }
    }

    private def sessionCookie(sessionInfo: SessionInfo): HttpCookie = HttpCookie(
        name = COOKIE_SESSION_ID,
        value = sessionInfo.sessionId,
        httpOnly = true,
        secure = true,
        path = Some("/"),
    ).withSameSite(headers.SameSite.Lax)
}

object AuthRoutes{
    val COOKIE_SESSION_ID = "GR_NOTES_SESSION_ID"
}