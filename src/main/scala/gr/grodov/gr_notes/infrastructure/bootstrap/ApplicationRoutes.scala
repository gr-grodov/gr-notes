package gr.grodov.gr_notes.infrastructure.bootstrap

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import gr.grodov.gr_notes.infrastructure.http.{GlobalExceptionHandler, GlobalRejectionHandler}

object ApplicationRoutes {

    def apply(components: ApplicationComponents): Route =
        handleExceptions(GlobalExceptionHandler.handler) {
            handleRejections(GlobalRejectionHandler.handler) {
                concat(
                    components.notesRoutes.routes,
                    components.authRoutes.routes
                )
            }
        }
}