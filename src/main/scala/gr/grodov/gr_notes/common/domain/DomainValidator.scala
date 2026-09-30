package gr.grodov.gr_notes.common.domain

import cats.data.{Validated, ValidatedNel}

trait DomainValidator[T, E <: DomainError] {
    type FieldValidationResult[A] = ValidatedNel[ErrorField, A]
    type ValidationResult[A] = Either[E, A]
    def validate(data: T): ValidationResult[T]

    def validateStringNotEmpty(data: String, field: String): FieldValidationResult[String] = {
        Validated.condNel(
            data.trim.nonEmpty,
            data.trim,
            ErrorField(field = field, code = "empty")
        )
    }
}