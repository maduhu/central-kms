package org.leveloneproject.central.kms.domain.batches

import java.util.UUID

case class CreateRequest(sidecarId: UUID, batchId: UUID, signature: String)
