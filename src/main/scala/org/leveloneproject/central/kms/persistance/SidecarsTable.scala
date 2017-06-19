package org.leveloneproject.central.kms.persistance

import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.domain.sidecars.Sidecar

trait SidecarsTable extends DataMappers {
  this: DbProfile ⇒

  import profile.api._

  class SidecarsTable(tag: Tag) extends Table[Sidecar](tag, "sidecars") {
    def id = column[UUID]("id")
    def serviceName = column[String]("service_name")
    def registered = column[Instant]("registered")
    def terminated = column[Option[Instant]]("terminated")
    def * = (id, serviceName, registered, terminated) <> (Sidecar.tupled, Sidecar.unapply)
  }

  val sidecars = TableQuery[SidecarsTable]
}
