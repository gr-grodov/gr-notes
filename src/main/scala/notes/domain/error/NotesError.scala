package notes.domain.error

import common.domain.{DomainError, ErrorField}

sealed trait NotesError extends DomainError

object NotesError {
    final case class NotFound() extends NotesError {
        override def code: String = "note_not_found"
    }

    final case class FailValidation(fieldsError: Seq[ErrorField]) extends NotesError {
        override def code: String = "validation_error"
        override def fields: Seq[ErrorField] = fieldsError
    }
}
