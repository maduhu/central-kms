package org.leveloneproject.central.kms.persistance

import java.sql.Timestamp
import java.time.Instant

import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

trait DataMappers {
  this: DbProfile ⇒

  import profile.api._

  implicit val instantMapper: JdbcType[Instant] with BaseTypedType[Instant] = MappedColumnType.base[Instant, Timestamp](
    i ⇒ Timestamp.from(i),
    ts ⇒ ts.toInstant
  )
}
