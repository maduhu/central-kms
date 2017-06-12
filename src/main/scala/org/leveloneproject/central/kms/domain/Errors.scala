package org.leveloneproject.central.kms.domain

import java.util.UUID

import org.leveloneproject.central.kms.sidecar.SideCarCommand

object Errors {
  def SidecarExistsError(id: UUID) = Error(110, "Sidecar with id '%s' already exists".format(id))
  def BatchExistsError(id: UUID) = Error(120, "Batch with id '%s' already exists".format(id))
  val InternalError = Error(-32603, "Internal error")

  def InternalCommandError(commandId: String) = ErrorWithCommandId(InternalError, commandId)
  val ParseError = Error(-32700, "Parse error")
  val InvalidRequest = Error(-32600, "Invalid Request")

  def InvalidParameters(commandId: String) = ErrorWithCommandId(Error(-32602, "Invalid params"), commandId)

  def MethodNotFound(commandId: String) = ErrorWithCommandId(Error(-32601, "Method not found"), commandId)

  def MethodNotAllowedInCurrentState(command: SideCarCommand) = ErrorWithCommandId(Error(100, "'%s' method not allowed in current state".format(command.method)), command.commandId)
}
