package org.leveloneproject.central.kms.socket

import org.json4s.JsonAST.JValue
import org.leveloneproject.central.kms.domain.Error

trait Output

case class RpcInput(jsonrpc: String, id: Option[String], method: String, params: JValue)

case class RpcError(jsonrpc: String = "2.0", error: Error, id: String = null) extends Output

case class RpcResponse(jsonrpc: String = "2.0", result: Any, id: String = null) extends Output

