package infrastructure.auth

import io.circe.Decoder

final case class OidcMetadata(
    issuer: String,

    authorizationEndpoint: String,
    tokenEndpoint: String,
    jwksUri: String,
    userinfoEndpoint: Option[String],
    endSessionEndpoint: Option[String],

    idTokenSigningAlgValuesSupported: List[String],
    codeChallengeMethodsSupported: List[String],
    tokenEndpointAuthMethodsSupported: List[String],
    scopesSupported: List[String]
)

object OidcMetadata {

    implicit val decoder: Decoder[OidcMetadata] = Decoder.instance { cursor =>
        for {
            issuer <- cursor.get[String]("issuer")

            authorizationEndpoint <- cursor.get[String]("authorization_endpoint")
            tokenEndpoint <- cursor.get[String]("token_endpoint")
            jwksUri <- cursor.get[String]("jwks_uri")
            userinfoEndpoint <- cursor.get[Option[String]]("userinfo_endpoint")
            endSessionEndpoint <- cursor.get[Option[String]]("end_session_endpoint")

            tokenEndpointAuthMethodsSupported <- cursor.get[List[String]]("token_endpoint_auth_methods_supported")
            codeChallengeMethodsSupported <- cursor.get[List[String]]("code_challenge_methods_supported")

            idTokenSigningAlgValuesSupported <- cursor.get[List[String]]("id_token_signing_alg_values_supported")
            scopesSupported <- cursor.get[List[String]]("scopes_supported")

        } yield OidcMetadata(
            issuer = issuer,

            authorizationEndpoint = authorizationEndpoint,
            tokenEndpoint = tokenEndpoint,
            jwksUri = jwksUri,
            userinfoEndpoint = userinfoEndpoint,
            endSessionEndpoint = endSessionEndpoint,

            tokenEndpointAuthMethodsSupported = tokenEndpointAuthMethodsSupported,
            codeChallengeMethodsSupported = codeChallengeMethodsSupported,

            idTokenSigningAlgValuesSupported = idTokenSigningAlgValuesSupported,
            scopesSupported = scopesSupported
        )
    }
}