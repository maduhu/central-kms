package org.leveloneproject.central.kms.domain

import java.time.Instant
import java.util.UUID

case class Sidecar(id: UUID, serviceName: String, registered: Instant, terminated: Option[Instant] = None)
