package auth.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import auth.service.AuthService

import scala.util.Success

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
            onSuccess(authService.callback(code, state)) { tokens =>
                complete(StatusCodes.OK)
            }
        }
    }
}
