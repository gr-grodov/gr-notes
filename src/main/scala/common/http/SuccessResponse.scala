package common.http

import io.circe.generic.auto._

final case class SuccessResponse[T](
    success: Boolean = true,
    message: String = "",
    data: Option[T] = None
)