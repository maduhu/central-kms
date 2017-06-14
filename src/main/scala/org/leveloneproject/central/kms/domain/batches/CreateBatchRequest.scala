package org.leveloneproject.central.kms.domain.batches

import java.util.UUID

case class CreateBatchRequest(sidecarId: UUID, batchId: UUID, signature: String)
