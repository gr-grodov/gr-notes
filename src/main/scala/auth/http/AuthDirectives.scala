package auth.http

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.provide

object AuthDirectives {

    // TODO хардкод добавить получение токенов с SSO
    private val StubOwnerSub = "dev-user"
    def currentUserSub: Directive1[String] = provide(StubOwnerSub)
}

