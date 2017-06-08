package org.leveloneproject.central.kms.sidecar

import java.util.UUID

import org.leveloneproject.central.kms.domain.CommandResponse

trait SideCarCommand {
  val commandId: String
  val method: String
}

case class RegisterCommand(commandId: String, params: RegisterParameters) extends SideCarCommand {
  val method = "register"
}

case class RegisterParameters(id: UUID, serviceName: String)

// Responses
object Registered {
  def apply(commandId: String, result: RegisteredResult) = CommandResponse(result, commandId)
}

case class RegisteredResult(id: UUID, batchKey: String, rowKey: String)
