package org.leveloneproject.central.kms.utils

import java.util.UUID

import org.leveloneproject.central.kms.routing.JsonSupport
import org.leveloneproject.central.kms.socket.RpcResponse

trait MessageBuilder extends JsonSupport {
  def batchRequest(
    requestId: String = UUID.randomUUID().toString,
    batchId: UUID = UUID.randomUUID(),
    signature: String = ""
    ): String =
    "{\"jsonrpc\":\"2.0\",\"id\":\"%s\",\"method\":\"batch\",\"params\":{\"id\":\"%s\",\"signature\":\"%s\"}}"
      .format(requestId, batchId, signature)

  def batchResponse(
   requestId: String = UUID.randomUUID().toString,
   batchId: UUID = UUID.randomUUID()
   ): String =
    "{\"jsonrpc\":\"2.0\",\"result\":{\"id\":\"%s\"},\"id\":\"%s\"}"
      .format(batchId, requestId)

  def registerRequest(
   requestId: String = UUID.randomUUID().toString,
   sidecarId: UUID = UUID.randomUUID(),
   serviceName: String = "service name"): String =
    "{\"jsonrpc\":\"2.0\",\"id\":\"%s\",\"method\":\"register\",\"params\":{\"id\":\"%s\",\"serviceName\":\"%s\"}}"
      .format(requestId, sidecarId, serviceName)

  def registerResponse(
    requestId: String = UUID.randomUUID().toString,
    sidecarId: UUID = UUID.randomUUID(),
    batchKey: String = "",
    rowKey: String = ""): String =
      "{\"jsonrpc\":\"2.0\",\"result\":{\"id\":\"%s\",\"batchKey\":\"%s\",\"rowKey\":\"%s\"},\"id\":\"%s\"}"
        .format(sidecarId, batchKey, rowKey, requestId)

  def healthCheckResponse(healthCheckId: UUID, result: Any): String = {
    write(RpcResponse(result = result, id = healthCheckId.toString))
  }
}
