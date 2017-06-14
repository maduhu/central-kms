package org.leveloneproject.central.kms.sidecar.batch

import java.util.UUID

import org.leveloneproject.central.kms.domain.batches.CreateBatchRequest
import org.leveloneproject.central.kms.sidecar.SideCarCommand

case class BatchParameters(id: UUID, signature: String) {
  def toCreateRequest(sidecarId: UUID) = CreateBatchRequest(sidecarId, id, signature)
}

case class BatchCommand(commandId: String, parameters: BatchParameters) extends SideCarCommand {
  val method: String = "batch"
}