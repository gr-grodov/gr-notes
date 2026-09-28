package notes.domain.repo

import notes.domain.Note
import slick.jdbc.PostgresProfile.api._

import java.time.Instant
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class DBNotesRepository(db: Database)(implicit ex: ExecutionContext) extends NotesRepository {
    import notes.domain.table.NotesTable.notes

    override def listByUser(userSub: String): Future[Seq[Note]] =
        db.run(notes
            .filter(_.userSub === userSub)
            .sortBy(_.updatedAt.desc)
            .result
        )

    override def find(id: UUID, userSub: String): Future[Option[Note]] =
        db.run(findQuery(id, userSub).result.headOption)

    override def create(userSub: String, title: String, content: String): Future[Note] = {
        val insertQuery = notes.map(note => (note.userSub, note.title, note.content)) returning notes
        db.run(insertQuery += (userSub, title, content))
    }

    override def update(id: UUID, userSub: String)(f: Note => Note): Future[Option[Note]] = {
        val query = for {
            note <- findQuery(id, userSub).result.headOption
            result <- note match {
                case Some(findNote) =>
                    val updatedNote = f(findNote).copy(id = id, userSub = userSub, updateAt = Instant.now())
                    findQuery(id, userSub).update(updatedNote).map(_ => Some(updatedNote))
                case None =>
                    DBIO.successful(None)
            }
        } yield result

        db.run(query.transactionally)
    }

    override def delete(id: UUID, userSub: String): Future[Int] =
        db.run(findQuery(id, userSub).delete)

    private def findQuery(id: UUID, userSub: String) =
        notes.filter(note => note.id === id && note.userSub === userSub)
}
