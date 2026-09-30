package auth.utils

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jwt.JWTParser
import com.nimbusds.oauth2.sdk.id.{ClientID, Issuer}
import com.nimbusds.openid.connect.sdk.Nonce
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator
import infrastructure.auth.{OidcConfig, OidcMetadata}

import java.net.{URI, URL}
import scala.concurrent.{ExecutionContext, Future}

final class IdTokenValidator(
    oidcConfig: OidcConfig,
    oidcMetadata: OidcMetadata
)(implicit ec: ExecutionContext) {

    private val validator = new IDTokenValidator(
        new Issuer(oidcConfig.issuer),
        new ClientID(oidcConfig.clientId),
        JWSAlgorithm.RS256,
        new URI(oidcMetadata.jwksUri).toURL
    )

    def validate(idToken: String, expectedNonce: String): Future[IdentityUser] = Future {
        val jwt = JWTParser.parse(idToken)
        val claims: IDTokenClaimsSet = validator.validate(jwt, new Nonce(expectedNonce))

        IdentityUser(
            subject = claims.getSubject.getValue,
            sid = claims.getSessionID.getValue
        )
    }
}

final case class IdentityUser(
    subject: String,
    sid: String
)