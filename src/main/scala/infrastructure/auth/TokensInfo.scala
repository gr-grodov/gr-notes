package infrastructure.auth

import io.circe.Decoder

final case class TokensInfo(
    accessToken: String,
    refreshToken: Option[String],
    tokenType: String,
    idToken: Option[String],
    scope: List[String],
)

object TokensInfo {
    implicit val decoder: Decoder[TokensInfo] =
        Decoder.instance { cursor =>
            for {
                accessToken <- cursor.get[String]("access_token")
                refreshToken <- cursor.get[Option[String]]("refresh_token")
                tokenType <- cursor.get[String]("token_type")
                idToken <- cursor.get[Option[String]]("idToken")
                scope <- cursor.get[List[String]]("scope")
            } yield TokensInfo(
                accessToken = accessToken,
                refreshToken = refreshToken,
                tokenType = tokenType,
                idToken = idToken,
                scope = scope
            )
        }
}