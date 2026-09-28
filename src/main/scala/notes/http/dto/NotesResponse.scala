package notes.http.dto

import notes.domain.Note

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