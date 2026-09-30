package gr.grodov.gr_notes.notes.http

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import gr.grodov.gr_notes.common.http.{ErrorHandler, ErrorResponse}
import gr.grodov.gr_notes.notes.domain.error.NotesError

object NotesErrorHandler extends ErrorHandler[NotesError] {

    override def handle(error: NotesError): (StatusCode, ErrorResponse) = error match {
        case NotesError.NotFound() => (
            StatusCodes.NotFound,
            ErrorResponse(error.code)
        )
        case NotesError.FailValidation(fieldsError) => (
            StatusCodes.BadRequest,
            ErrorResponse(error.code, fieldsError)
        )
    }
}
