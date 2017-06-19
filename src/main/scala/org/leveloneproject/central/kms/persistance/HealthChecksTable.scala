package org.leveloneproject.central.kms.persistance

import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheck, HealthCheckLevel, HealthCheckStatus}

trait HealthChecksTable extends DataMappers {
  this: DbProfile ⇒

  import profile.api._

  class HealthChecksTable(tag: Tag) extends Table[HealthCheck](tag, "healthchecks") {
    def id = column[UUID]("id")
    def sidecarId = column[UUID]("sidecar_id")
    def level = column[HealthCheckLevel]("level")
    def created = column[Instant]("created")
    def status = column[HealthCheckStatus]("status")
    def responded = column[Option[Instant]]("responded")
    def response = column[Option[String]]("response")

    def * = (id, sidecarId, level, created, status, responded, response) <> (HealthCheck.tupled, HealthCheck.unapply)
  }

  val healthChecks = TableQuery[HealthChecksTable]
}
