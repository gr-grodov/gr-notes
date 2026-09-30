package auth.service

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.Uri
import akka.util.Timeout
import auth.actor.OidcLoginTransactionActor.Stored
import auth.actor.{OidcLoginTransaction, OidcLoginTransactionActor}
import auth.utils.{IdTokenValidator, IdentityUser, PKCEUtils}
import common.logs.Logging
import infrastructure.auth.{OidcConfig, OidcMetadata}

import java.time.Instant
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

final class AuthService (
    oidcClientService: OidcClientService,
    sessionService: SessionService,
    idTokenValidator: IdTokenValidator,
    loginTransactionActor: ActorRef[OidcLoginTransactionActor.Command]
) (implicit system: ActorSystem[_], ec: ExecutionContext) {

    private implicit val askTimeout: Timeout = 3.seconds

    def ssoLoginURI: Future[Uri] = {
        val state = PKCEUtils.generateState()
        val nonce = PKCEUtils.generateNonce()
        val codeVerifier = PKCEUtils.generateVerifier()
        val codeChallenge = PKCEUtils.generateChallenge(codeVerifier)

        startLogin(state, nonce, codeVerifier).flatMap {
            case OidcLoginTransactionActor.Stored => Future.successful(oidcClientService.buildAuthorizationUri(state, nonce, codeChallenge))
            case OidcLoginTransactionActor.AlreadyExists => Future.failed(AuthException.AlreadyExistStateException())
        }
    }

    def authenticate(code: String, state: String): Future[SessionInfo] = {
        loginTransactionActor.ask[OidcLoginTransactionActor.ConsumeResult] { replyTo =>
            OidcLoginTransactionActor.Consume(state, replyTo)
        }.flatMap {
            case OidcLoginTransactionActor.ConsumeResult(Some(transaction)) => getAuthTokensByLoginTransaction(code, transaction)
            case _ => Future.failed(AuthException.InvalidOidcStateException())
        }.flatMap { authTokens => sessionService.createSession(authTokens)}
    }

    private def startLogin(state: String, nonce: String, codeVerifier: String): Future[OidcLoginTransactionActor.StoreResult] = {
        val transaction = OidcLoginTransaction(
            state = state,
            nonce = nonce,
            codeVerifier = codeVerifier,
            createdAt = Instant.now()
        )

        loginTransactionActor.ask[OidcLoginTransactionActor.StoreResult] { replyTo =>
            OidcLoginTransactionActor.Store(transaction, replyTo)
        }
    }

    private def getAuthTokensByLoginTransaction(code: String, transaction: OidcLoginTransaction): Future[AuthenticatedTokens] = {
        oidcClientService.exchangeCode(code = code, codeVerifier = transaction.codeVerifier).flatMap { tokens =>
            tokens.idToken match {
                case Some(idToken) => idTokenValidator.validate(idToken, transaction.nonce).map(
                    identity => AuthenticatedTokens(tokens, identity)
                )
                case None => Future.failed(AuthException.MissingIdTokenException())
            }
        }
    }
}


sealed abstract class AuthException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

object AuthException {
    final case class InvalidOidcStateException() extends AuthException("Invalid oidc state")
    final case class MissingIdTokenException() extends AuthException("Missing id_token in response")
    final case class AlreadyExistStateException() extends AuthException("State for OIDC is already exist")
}

final case class AuthenticatedTokens(
    tokens: TokensInfo,
    identityUser: IdentityUser
)