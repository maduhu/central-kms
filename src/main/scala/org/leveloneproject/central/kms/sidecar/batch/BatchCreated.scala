package org.leveloneproject.central.kms.sidecar.batch

import java.util.UUID

import org.leveloneproject.central.kms.domain.CommandResponse

object BatchCreated {
  def apply(commandId: String, result: BatchCreatedResult) = CommandResponse(result, commandId)
}

case class BatchCreatedResult(id: UUID)
