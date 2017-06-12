package org.leveloneproject.central.kms.sidecar.registration

import org.leveloneproject.central.kms.domain.CommandResponse

object Registered {
  def apply(commandId: String, result: RegisteredResult) = CommandResponse(result, commandId)
}
