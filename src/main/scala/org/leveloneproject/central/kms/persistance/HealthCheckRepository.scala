package org.leveloneproject.central.kms.persistance

import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.domain.healthchecks.HealthCheck

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait HealthCheckRepository extends HealthChecksTable {
  this: DbProfile ⇒

  val dbProvider: DbProvider

  import profile.api._

  private val db = dbProvider.db

  def insert(healthCheck: HealthCheck): Future[Unit] = db.run { healthChecks += healthCheck } map { _ ⇒ }

  def complete(healthCheckId: UUID, response: String, timestamp: Instant): Future[Option[HealthCheck]] = {

    val filter = healthChecks.filter(_.id === healthCheckId)

    val update = filter.map(h ⇒ (h.responded, h.response))
        .update((Some(timestamp), Some(response)))

    db.run(update).flatMap(_ ⇒ db.run(filter.result.headOption))

  }
}
