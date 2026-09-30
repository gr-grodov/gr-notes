package gr.grodov.gr_notes.common.http

import akka.http.scaladsl.model.StatusCode
import gr.grodov.gr_notes.common.domain.DomainError

trait ErrorHandler[E <: DomainError] {
    def handle(error: E): (StatusCode, ErrorResponse)
}
