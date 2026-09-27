package common.http

import io.circe.generic.auto._

final case class ErrorResponse(
    code: String,
    message: String = "",
    errors: Seq[ErrorFieldDto] = Nil
)

final case class ErrorFieldDto(field: String, code: String)