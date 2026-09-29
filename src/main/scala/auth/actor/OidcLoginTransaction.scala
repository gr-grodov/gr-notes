package auth.actor

import java.time.Instant

final case class OidcLoginTransaction(
    state: String,
    nonce: String,
    codeVerifier: String,
    createdAt: Instant
)
