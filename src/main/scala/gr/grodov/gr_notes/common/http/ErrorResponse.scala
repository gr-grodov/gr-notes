package gr.grodov.gr_notes.common.http

import gr.grodov.gr_notes.common.domain.ErrorField

final case class ErrorResponse(
    code: String,
    fields: Seq[ErrorField] = Nil
)