package gr.grodov.gr_notes.auth.http

import akka.http.javadsl.model.headers
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.HttpCookie
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import AuthRoutes.COOKIE_SESSION_ID
import gr.grodov.gr_notes.auth.service.{AuthService, SessionInfo}

final class AuthRoutes(authService: AuthService) {
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
        secure = false,
        path = Some("/"),
    ).withSameSite(headers.SameSite.Lax)
}

object AuthRoutes{
    val COOKIE_SESSION_ID = "GR_NOTES_SESSION_ID"
}