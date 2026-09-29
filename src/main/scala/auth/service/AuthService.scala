package auth.service

import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.Uri
import akka.util.Timeout
import auth.actor.{OidcLoginTransaction, OidcLoginTransactionActor}
import auth.utils.PKCEUtils
import common.logs.Logging
import infrastructure.auth.{OidcConfig, OidcMetadata}

import java.time.Instant
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

final class AuthService (
    oidcConfig: OidcConfig,
    oidcMetadata: OidcMetadata,
    loginTransactionActor: ActorRef[OidcLoginTransactionActor.Command]
) (implicit system: ActorSystem[_], ec: ExecutionContext) {

    private implicit val askTimeout: Timeout = 3.seconds

    def getSSOAuthorizationUri: Future[Uri] = {
        val state = PKCEUtils.generateState()
        val nonce = PKCEUtils.generateNonce()
        val codeVerifier = PKCEUtils.generateVerifier()
        val codeChallenge = PKCEUtils.generateChallenge(codeVerifier)

        startLogin(state, nonce, codeVerifier).map(_ => buildAuthorizationUri(state, nonce, codeChallenge))
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

    private def buildAuthorizationUri(state: String, nonce: String, codeChallenge: String): Uri =
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
}
