package infrastructure.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.Directives._
import common.http.ErrorResponse
import common.logs.Logging

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
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
