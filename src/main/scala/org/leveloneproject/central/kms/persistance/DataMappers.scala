package org.leveloneproject.central.kms.persistance

import java.sql.Timestamp
import java.time.Instant

import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheckLevel, HealthCheckStatus}
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

trait DataMappers {
  this: DbProfile ⇒

  import profile.api._

  implicit val instantMapper: JdbcType[Instant] with BaseTypedType[Instant] = MappedColumnType.base[Instant, Timestamp](
    i ⇒ Timestamp.from(i),
    ts ⇒ ts.toInstant
  )

  implicit val healthCheckLevelMapper: JdbcType[HealthCheckLevel] with BaseTypedType[HealthCheckLevel] = MappedColumnType.base[HealthCheckLevel, Int](
    l ⇒ l.id,
    s ⇒ HealthCheckLevel(s)
  )

  implicit val healthCheckStatusMapper = MappedColumnType.base[HealthCheckStatus, Int](
    status ⇒ status.id,
    status ⇒ HealthCheckStatus(status)
  )
}