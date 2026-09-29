package auth.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import auth.service.AuthService

import scala.util.Success

final class AuthRoutes(authService: AuthService) {
    val routes: Route = pathPrefix("api" / "auth") {
        path("login") {
            get {
                login()
            }
        }
    }

    private def login(): Route = {
        onComplete(authService.getSSOAuthorizationUri) {
            case Success(uri) => redirect(uri, StatusCodes.Found)
        }
    }
}
