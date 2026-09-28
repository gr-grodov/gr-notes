package common.http

import akka.http.scaladsl.model.StatusCode
import common.domain.DomainError

trait ErrorHandler[E <: DomainError] {
    def handle(error: E): (StatusCode, ErrorResponse)
}
