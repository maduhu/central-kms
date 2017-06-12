package org.leveloneproject.central.kms.domain

import java.time.Instant
import java.util.UUID

case class Batch(id: UUID, sidecarId: UUID, signature: String, timestamp: Instant)
