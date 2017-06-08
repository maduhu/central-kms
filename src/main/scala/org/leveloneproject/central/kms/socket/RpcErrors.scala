package org.leveloneproject.central.kms.socket

import org.leveloneproject.central.kms.domain.{Error, ErrorWithCommandId}

object RpcErrors {
  val ParseError = Error(-32700, "Parse error")
  val InvalidRequest = Error(-32600, "Invalid Request")

  def InvalidParameters(commandId: String) = ErrorWithCommandId(Error(-32602, "Invalid params"), commandId)

  def MethodNotFound(commandId: String) = ErrorWithCommandId(Error(-32601, "Method not found"), commandId)

  val InternalError = Error(-32603, "Internal error")

  def InternalCommandError(commandId: String) = ErrorWithCommandId(InternalError, commandId)
}
