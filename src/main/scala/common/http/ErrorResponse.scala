package common.http

import common.domain.ErrorField

final case class ErrorResponse(
    code: String,
    fields: Seq[ErrorField] = Nil
)