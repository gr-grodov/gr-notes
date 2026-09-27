package infrastructure.auth

final case class OidcConfig(
    issuer: String,
    authorizationEndpoint: String,
    tokenEndpoint: String,
    jwksUri: String,
    userinfoEndpoint: String,
    endSessionEndpoint: String,
    clientId: String,
    clientSecret: String,
    redirectUri: String,
    scopes: List[String]
)