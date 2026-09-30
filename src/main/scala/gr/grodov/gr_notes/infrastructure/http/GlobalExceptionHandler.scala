package gr.grodov.gr_notes.infrastructure.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import gr.grodov.gr_notes.common.http.ErrorResponse
import gr.grodov.gr_notes.common.logs.Logging
import io.circe.generic.auto._

object GlobalExceptionHandler extends Logging{

    val handler: ExceptionHandler =
        ExceptionHandler {
            case ex: Throwable =>
                logger.error(ex.getMessage, ex)
                complete(
                    StatusCodes.InternalServerError,
                    ErrorResponse(code = "internal_error")
                )
        }
}
