package common.domain

import cats.data.{Validated, ValidatedNel}

trait DomainValidator[T, E <: DomainError] {
    type ValidationFieldResult[A] = ValidatedNel[ErrorField, A]
    type ValidationResult[A] = Either[E, A]
    def validate(data: T): ValidationResult[T]

    def validateStringNotEmpty(data: String, field: String): ValidationFieldResult[String] = {
        Validated.condNel(
            data.trim.nonEmpty,
            data.trim,
            ErrorField(field = field, code = "empty")
        )
    }
}