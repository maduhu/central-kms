package org.leveloneproject.central.kms.socket

import org.json4s.JsonAST.JValue
import org.leveloneproject.central.kms.domain.Error

trait Input
trait Output

case class SocketRequest(jsonrpc: String, id: String, method: String, params: JValue) extends Input

case class SocketResponse(jsonrpc: String, result: Option[JValue], error: Option[JValue], id: String) extends Input

case class RpcError(jsonrpc: String = "2.0", error: Error, id: String = null) extends Output

case class RpcRequest(jsonrpc: String = "2.0", id: String, method: String, params: Any) extends Output

case class RpcResponse(jsonrpc: String = "2.0", result: Any, id: String = null) extends Output

