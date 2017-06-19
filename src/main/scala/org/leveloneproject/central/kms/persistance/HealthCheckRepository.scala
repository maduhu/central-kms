package org.leveloneproject.central.kms.persistance

import org.leveloneproject.central.kms.domain.healthchecks.HealthCheck

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait HealthCheckRepository extends HealthChecksTable {
  this: DbProfile ⇒

  val dbProvider: DbProvider

  import profile.api._

  private val db = dbProvider.db

  def insert(healthCheck: HealthCheck): Future[Unit] = db.run { healthChecks += healthCheck } map { _ ⇒ }
}
