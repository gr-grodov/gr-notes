package gr.grodov.gr_notes.notes.http.dto

import gr.grodov.gr_notes.notes.domain.Note

import java.time.Instant
import java.util.UUID

final case class NoteResponse(
    id: UUID,
    title: String,
    content: String,
    createdAt: Instant,
    updatedAt: Instant
)

object NoteResponse {
    def fromDomain(note: Note): NoteResponse =
        NoteResponse(note.id, note.title, note.content, note.createdAt, note.updateAt)
}