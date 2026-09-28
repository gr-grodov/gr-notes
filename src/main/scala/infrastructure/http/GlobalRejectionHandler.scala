package infrastructure.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{AuthenticationFailedRejection, AuthorizationFailedRejection, MethodRejection, MissingHeaderRejection, MissingQueryParamRejection, RejectionHandler, ValidationRejection}
import common.domain.ErrorField
import common.http.ErrorResponse
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

object GlobalRejectionHandler {

    val handler: RejectionHandler = RejectionHandler
        .newBuilder()
        .handle {
            case AuthenticationFailedRejection(_, _) =>
                complete(
                    StatusCodes.Unauthorized,
                    ErrorResponse(code = "unauthorized")
                )
        }
        .handle {
            case AuthorizationFailedRejection =>
                complete(
                    StatusCodes.Forbidden,
                    ErrorResponse(code = "forbidden")
                )
        }
        .handle {
            case MethodRejection(_) =>
                complete(
                    StatusCodes.MethodNotAllowed,
                    ErrorResponse(code = "method_not_allowed")
                )
        }
        .handleNotFound {
            complete(
                StatusCodes.NotFound,
                ErrorResponse(code = "not_found")
            )
        }
        .result()
}
