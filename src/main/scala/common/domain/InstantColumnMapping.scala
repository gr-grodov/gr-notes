package common.domain

import java.sql.Timestamp
import java.time.Instant

import slick.jdbc.PostgresProfile.api._

trait InstantColumnMapping {
    implicit val instantColumnType: BaseColumnType[Instant] =
        MappedColumnType.base[Instant, Timestamp](Timestamp.from, _.toInstant)
}
