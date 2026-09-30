package gr.grodov.gr_notes.notes.domain.table

import gr.grodov.gr_notes.common.domain.InstantColumnMapping
import gr.grodov.gr_notes.notes.domain.Note
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

class NotesTable(tag: Tag) extends Table[Note](tag, "gr/grodov/gr_notes/notesodov/gr_notes/notes") with InstantColumnMapping {

    def id = column[UUID]("id", O.PrimaryKey)
    def userSub = column[String]("user_sub")
    def title = column[String]("title")
    def content = column[String]("content")
    def createdAt = column[Instant]("created_at")
    def updatedAt = column[Instant]("updated_at")

    override def * : ProvenShape[Note] = (id, userSub, title, content, createdAt, updatedAt).mapTo[Note]
}

object NotesTable {
    val notes = TableQuery[NotesTable]
}
