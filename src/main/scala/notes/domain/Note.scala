package notes.domain

import java.time.Instant
import java.util.UUID

final case class Note(
    id: UUID,
    userSub: String,
    title: String,
    content: String,
    createdAt: Instant,
    updateAt: Instant
)
