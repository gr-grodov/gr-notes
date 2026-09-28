package infrastructure.auth

final case class OidcConfig(
    issuer: String,
    clientId: String,
    clientSecret: String,
    redirectUri: String,
    scopes: List[String]
)