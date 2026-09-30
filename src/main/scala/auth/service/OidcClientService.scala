package auth.service

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.util.ByteString
import infrastructure.auth.{OidcConfig, OidcMetadata}
import io.circe.parser.{decode, parse}

import scala.concurrent.{ExecutionContext, Future}

final class OidcClientService(
    oidcConfig: OidcConfig,
    oidcMetadata: OidcMetadata
)(implicit system: ActorSystem[_], ec: ExecutionContext) {

    def exchangeCode(code: String, codeVerifier: String): Future[TokensInfo] = {
        val form = FormData(
            "grant_type" -> "authorization_code",
            "redirect_uri" -> oidcConfig.redirectUri,
            "code" -> code,
            "code_verifier" -> codeVerifier
        )
        executeTokenRequest(form)
    }

    def refresh(refreshToken: String): Future[TokensInfo] = {
        val form = FormData(
            "grant_type" -> "refresh_token",
            "refresh_token" -> refreshToken
        )
        executeTokenRequest(form)
    }

    def buildAuthorizationUri(state: String, nonce: String, codeChallenge: String): Uri =
        Uri(oidcMetadata.authorizationEndpoint).withQuery(
            Uri.Query(
                "response_type" -> "code",
                "client_id" -> oidcConfig.clientId,
                "redirect_uri" -> oidcConfig.redirectUri,
                "scope" -> oidcConfig.scopes.mkString(" "),
                "state" -> state,
                "nonce" -> nonce,
                "code_challenge" -> codeChallenge,
                "code_challenge_method" -> "S256"
            )
        )

    private def executeTokenRequest(form: FormData): Future[TokensInfo] = {
        val request = HttpRequest(
            method = HttpMethods.POST,
            uri = oidcMetadata.tokenEndpoint,
            entity = form.toEntity
        ).withHeaders(Authorization(BasicHttpCredentials(oidcConfig.clientId, oidcConfig.clientSecret)))

        Http()
            .singleRequest(request)
            .flatMap(handleResponse)
    }

    private def handleResponse(response: HttpResponse): Future[TokensInfo] = response.entity.dataBytes
        .runFold(ByteString.empty)(_ ++ _)
        .flatMap { bytes =>
            val body = bytes.utf8String
            if (!response.status.isSuccess()) {
                Future.failed(OidcClientException.TokenEndpointError(status = response.status.intValue, error = extractOAuthError(body)))
            } else {
                decode[TokensInfo](body) match {
                    case Right(tokens) => Future.successful(tokens)
                    case Left(error) => Future.failed(OidcClientException.InvalidTokenResponse(error))
                }
            }
        }

    private def extractOAuthError(body: String): Option[String] = {
        parse(body).toOption.flatMap(_.hcursor.get[String]("error").toOption)
    }
}

sealed abstract class OidcClientException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

object OidcClientException {

    final case class TokenEndpointError(
        status: Int,
        error: Option[String]
    ) extends OidcClientException(s"OIDC token endpoint returned HTTP $status: $error")

    final case class InvalidTokenResponse(
        cause: Throwable
    ) extends OidcClientException("Invalid OIDC token response", cause)

    final case class UnsupportedAuthenticationMethod(
        supported: List[String]
    ) extends OidcClientException(s"Unsupported token endpoint authentication methods: " + supported.mkString(", "))
}