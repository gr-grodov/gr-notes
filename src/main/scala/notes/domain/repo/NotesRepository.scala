package notes.domain.repo

import notes.domain.Note

import java.util.UUID
import scala.concurrent.Future

trait NotesRepository {
    def listByUser(userSub: String): Future[Seq[Note]]
    def find(id: UUID, userSub: String): Future[Option[Note]]
    def create(userSub: String, title: String, content: String): Future[Note]
    def update(id: UUID, userSub: String)(f: Note => Note): Future[Option[Note]]
    def delete(id: UUID, userSub: String): Future[Int]
}