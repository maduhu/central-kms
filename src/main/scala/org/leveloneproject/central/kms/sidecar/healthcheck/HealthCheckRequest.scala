package org.leveloneproject.central.kms.sidecar.healthcheck

import java.util.UUID

import org.leveloneproject.central.kms.domain.CommandRequest
import org.leveloneproject.central.kms.domain.healthchecks.HealthCheckLevel

case object HealthCheckRequest {
  def apply(id: UUID, level: HealthCheckLevel) = CommandRequest(id.toString, "healthcheck", HealthCheckRequestParameters(level))
}

case class HealthCheckRequestParameters(level: HealthCheckLevel)