package gr.grodov.gr_notes.notes.service

final case class CreateNoteCommand(
    title: String,
    content: String
)

final case class UpdateNoteCommand(
    title: String,
    content: String
)