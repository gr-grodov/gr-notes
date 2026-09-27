package notes.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import common.http.SuccessResponse

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

object NotesRoutes {

    private val healthRoute: Route = path("notes") {
        get {
            complete(SuccessResponse(
                message = "Notes endpoint works",
                data = Some(Seq("note-1", "note-2"))
            ))
        }
    }

    val routes: Route = pathPrefix("api") {
        concat(healthRoute)
    }
}
