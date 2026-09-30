package gr.grodov.gr_notes.notes.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import gr.grodov.gr_notes.auth.http.AuthDirectives
import gr.grodov.gr_notes.common.http.{ErrorHandler, RouteErrorHandling}
import gr.grodov.gr_notes.notes.domain.error.NotesError
import gr.grodov.gr_notes.notes.http.dto.{CreateNoteRequest, UpdateNoteRequest}
import gr.grodov.gr_notes.notes.service.NotesService
import io.circe.generic.auto._

class NotesRoutes(notesService: NotesService, authDirectives: AuthDirectives) extends RouteErrorHandling[NotesError] {
    override def errorHandler: ErrorHandler[NotesError] = NotesErrorHandler

    val routes: Route = pathPrefix("api" / "gr/grodov/gr_notes/notesodov/gr_notes/notes") {
        authDirectives.authenticated { user =>
            concat(
                getNotes(user.userSub),
                getNote(user.userSub),
                createNote(user.userSub),
                updateNote(user.userSub),
                deleteNote(user.userSub)
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
