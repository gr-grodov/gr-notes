package gr.grodov.gr_notes.notes.http.dto

import gr.grodov.gr_notes.notes.service.{CreateNoteCommand, UpdateNoteCommand}

final case class CreateNoteRequest(title: String, content: String) {
    def toCommand: CreateNoteCommand = CreateNoteCommand(title, content)
}

final case class UpdateNoteRequest(
    title: String,
    content: String
) {
    def toCommand: UpdateNoteCommand = UpdateNoteCommand(title, content)
}