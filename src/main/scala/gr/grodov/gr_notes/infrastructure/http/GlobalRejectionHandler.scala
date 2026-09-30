package gr.grodov.gr_notes.infrastructure.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{AuthorizationFailedRejection, MethodRejection, RejectionHandler}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import gr.grodov.gr_notes.auth.http.MissingOrInvalidSession
import gr.grodov.gr_notes.common.http.ErrorResponse
import io.circe.generic.auto._

object GlobalRejectionHandler {

    val handler: RejectionHandler = RejectionHandler
        .newBuilder()
        .handle {
            case MissingOrInvalidSession =>
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
