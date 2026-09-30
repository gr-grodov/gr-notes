package gr.grodov.gr_notes.common.http

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.data.EitherT

import scala.concurrent.Future
import scala.util.{Failure, Success}

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import gr.grodov.gr_notes.common.domain.DomainError
import io.circe.generic.auto._

trait RouteErrorHandling[E <: DomainError] {
    protected def errorHandler: ErrorHandler[E]

    protected def completeResult[A](
        result: EitherT[Future, E, A],
        successStatus: StatusCode = StatusCodes.OK
    ) (implicit marshaller: ToEntityMarshaller[A], errorMarshaller: ToEntityMarshaller[ErrorResponse]): Route = onComplete(result.value) {
        case Success(Right(value)) => complete(successStatus, value)
        case Success(Left(error)) =>
            val (status, response) = errorHandler.handle(error)
            complete(status, response)
        case Failure(ex) => failWith(ex)
    }

    protected def completeFuture[A](
        result: Future[A],
        status: StatusCode = StatusCodes.OK
    ) (implicit marshaller: ToEntityMarshaller[A]): Route = onComplete(result) {
        case Success(value) => complete(status, value)
        case Failure(ex) => failWith(ex)
    }
}
