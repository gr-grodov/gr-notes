package gr.grodov.gr_notes.auth.service

import io.circe.Decoder

final case class TokensInfo(
    accessToken: String,
    refreshToken: Option[String],
    tokenType: String,
    idToken: Option[String],
    scope: Option[Seq[String]],
    expireIn: Long
)

object TokensInfo {
    implicit val decoder: Decoder[TokensInfo] = Decoder.instance { cursor =>
        for {
            accessToken <- cursor.get[String]("access_token")
            refreshToken <- cursor.get[Option[String]]("refresh_token")
            tokenType <- cursor.get[String]("token_type")
            idToken <- cursor.get[Option[String]]("id_token")
            scope <- cursor.get[Option[String]]("scope")
            expireIn <- cursor.get[Long]("expires_in")
        } yield TokensInfo(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenType = tokenType,
            idToken = idToken,
            scope = scope.map(_.split("\\s+")),
            expireIn = expireIn
        )
    }
}