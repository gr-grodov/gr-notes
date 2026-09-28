package infrastructure.auth

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.util.ByteString
import io.circe.parser.decode

import scala.concurrent.{ExecutionContext, Future}

final class OidcDiscovery(config: OidcConfig)(implicit system: ActorSystem[_], ec: ExecutionContext) {
    def discovery(): Future[OidcMetadata] = {
        val issuer = config.issuer.stripSuffix("/")
        val discoveryURI = s"$issuer/.well-known/openid-configuration"
        Http()
            .singleRequest(HttpRequest(uri = discoveryURI))
            .flatMap(handleResponse)
            .flatMap(validateMetadata)
    }

    private def handleResponse(response: HttpResponse): Future[OidcMetadata] = {
        response.entity.dataBytes
            .runFold(ByteString.empty)(_ ++ _)
            .flatMap { bytes =>
                if (!response.status.isSuccess()) {
                    Future.failed(OidcDiscoveryException.HttpError(response.status.intValue()))
                } else {
                    decode[OidcMetadata](bytes.utf8String) match {
                        case Right(metadata) => Future.successful(metadata)
                        case Left(error) => Future.failed(OidcDiscoveryException.InvalidResponse(error))
                    }
                }
            }
    }

    private def validateMetadata(metadata: OidcMetadata): Future[OidcMetadata] = {
        val validations = Seq(
            validate(
                "issuer",
                config.issuer,
                metadata.issuer
            )(_ == _),

            validate(
                "token_endpoint_auth_methods_supported",
                "client_secret_basic",
                metadata.tokenEndpointAuthMethodsSupported
            )(_.contains(_)),

            validate(
                "code_challenge_methods_supported",
                "client_secret_basic",
                metadata.codeChallengeMethodsSupported
            )(_.contains(_)),

            validate(
                "id_token_signing_alg_values_supported",
                "RS256",
                metadata.idTokenSigningAlgValuesSupported
            )(_.contains(_)),

            validate(
                "scope",
                "openid",
                metadata.scopesSupported
            )(_.contains(_))
        )

        validations.collectFirst {
            case Left(error) => Future.failed[OidcMetadata](error)
        }.getOrElse(Future.successful(metadata))
    }


    private def validate[A](
        param: String,
        expected: String,
        actual: A
    )(predicate: (A, String) => Boolean): Either[OidcDiscoveryException, Unit] =
        Either.cond(
            predicate(actual, expected),
            (),
            OidcDiscoveryException.InvalidMetadataParam(param, expected, actual.toString)
        )
}


sealed abstract class OidcDiscoveryException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

object OidcDiscoveryException {
    final case class HttpError(
        status: Int
    ) extends OidcDiscoveryException(s"OIDC discovery returned HTTP $status")

    final case class InvalidResponse(
        error: Throwable
    ) extends OidcDiscoveryException("Invalid OIDC discovery response", error)

    final case class InvalidMetadataParam(
        param: String,
        expected: String,
        actual: String
    ) extends OidcDiscoveryException(s"OIDC metadata param ($param) invalid: expected=$expected, actual=$actual")
}