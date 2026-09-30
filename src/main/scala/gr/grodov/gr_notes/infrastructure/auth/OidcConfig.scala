package gr.grodov.gr_notes.infrastructure.auth

final case class OidcConfig(
    issuer: String,
    clientId: String,
    clientSecret: String,
    redirectUri: String,
    scopes: List[String]
)