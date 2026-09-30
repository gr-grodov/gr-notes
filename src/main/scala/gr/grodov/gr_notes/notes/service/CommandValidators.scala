package gr.grodov.gr_notes.notes.service

import cats.implicits.{catsSyntaxTuple2Semigroupal, toBifunctorOps}
import gr.grodov.gr_notes.common.domain.DomainValidator
import gr.grodov.gr_notes.notes.domain.error.NotesError


object CreateNoteCommandValidator extends DomainValidator[CreateNoteCommand, NotesError] {
    override def validate(command: CreateNoteCommand): ValidationResult[CreateNoteCommand] = {
        (
            validateStringNotEmpty(command.title, "title"),
            validateStringNotEmpty(command.content, "content")
        )
            .mapN(CreateNoteCommand.apply)
            .toEither
            .leftMap(errors => NotesError.FailValidation(errors.toList))
    }
}

object UpdateNoteCommandValidator extends DomainValidator[UpdateNoteCommand, NotesError] {
    override def validate(command: UpdateNoteCommand): ValidationResult[UpdateNoteCommand] = {
        (
            validateStringNotEmpty(command.title, "title"),
            validateStringNotEmpty(command.content, "content")
        )
            .mapN(UpdateNoteCommand.apply)
            .toEither
            .leftMap(errors => NotesError.FailValidation(errors.toList))
    }
}
