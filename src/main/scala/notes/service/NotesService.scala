package notes.service

import cats.data.EitherT
import notes.domain.Note
import notes.domain.error.NotesError
import notes.domain.repo.NotesRepository

import scala.concurrent.{ExecutionContext, Future}

import java.util.UUID

class NotesService(repo: NotesRepository)(implicit ex: ExecutionContext) {

    def list(userSub: String): Future[Seq[Note]] =
        repo.listByUser(userSub)

    def get(id: UUID, userSub: String): EitherT[Future, NotesError, Note] =
        EitherT.fromOptionF(
            repo.find(id, userSub),
            NotesError.NotFound()
        )

    def create(userSub: String, command: CreateNoteCommand): EitherT[Future, NotesError, Note] = {
        for {
            validCommand <- EitherT.fromEither[Future](
                CreateNoteCommandValidator.validate(command)
            )
            note <- EitherT.liftF(
                repo.create(userSub = userSub, title = validCommand.title, content = validCommand.content)
            )
        } yield note
    }

    def update(id: UUID, userSub: String, command: UpdateNoteCommand): EitherT[Future, NotesError, Note] = {
        for {
            validCommand <- EitherT.fromEither[Future](
                UpdateNoteCommandValidator.validate(command)
            )
            note <- EitherT.fromOptionF[Future, NotesError, Note](
                repo.update(id, userSub) { updatingNote =>
                    updatingNote.copy(title = validCommand.title, content = validCommand.content)
                },
                NotesError.NotFound()
            )
        } yield note
    }

    def delete(id: UUID, userSub: String): EitherT[Future, NotesError, Boolean] =
        EitherT(repo.delete(id, userSub).map {
            case 0 => Left(NotesError.NotFound())
            case _ => Right(true)
        })
}
