package notes.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import auth.http.AuthDirectives.currentUserSub
import common.http.{ErrorHandler, RouteErrorHandling}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import notes.domain.error.NotesError
import notes.http.dto.{CreateNoteRequest, UpdateNoteRequest}
import notes.service.NotesService

import scala.concurrent.ExecutionContext

class NotesRoutes(notesService: NotesService)(implicit ec: ExecutionContext) extends RouteErrorHandling[NotesError] {
    override def errorHandler: ErrorHandler[NotesError] = NotesErrorHandler

    val routes: Route = pathPrefix("api" / "notes") {
        currentUserSub { userSub =>
            concat(
                getNotes(userSub),
                getNote(userSub),
                createNote(userSub),
                updateNote(userSub),
                deleteNote(userSub)
            )
        }
    }

    private def getNotes(userSub: String): Route =
        pathEndOrSingleSlash {
            get {
                completeFuture(notesService.list(userSub))
            }
        }

    private def getNote(userSub: String): Route =
        path(JavaUUID) { id =>
            get {
                completeResult(notesService.get(id, userSub))
            }
        }

    private def createNote(userSub: String): Route =
        pathEndOrSingleSlash {
            post {
                entity(as[CreateNoteRequest]) { request =>
                    completeResult(
                        notesService.create(userSub, request.toCommand),
                        StatusCodes.Created
                    )
                }
            }
        }

    private def updateNote(userSub: String): Route =
        path(JavaUUID) { id =>
            put {
                entity(as[UpdateNoteRequest]) { request =>
                    completeResult(notesService.update(id, userSub, request.toCommand))
                }
            }
        }

    private def deleteNote(userSub: String): Route =
        path(JavaUUID) { id =>
            delete {
                completeResult(notesService.delete(id, userSub))
            }
        }

}
