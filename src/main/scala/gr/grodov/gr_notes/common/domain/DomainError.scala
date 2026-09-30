package gr.grodov.gr_notes.common.domain

trait DomainError {
    def code: String
    def fields: Seq[ErrorField] = Nil
}

final case class ErrorField(field: String, code: String)
