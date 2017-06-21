package org.leveloneproject.central.kms.socket

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.routing.JsonSupport
import org.leveloneproject.central.kms.sidecar.SidecarMessageConverter

trait OutputConverter extends SidecarMessageConverter with JsonSupport {
  def toMessage(value: Any): Option[Message] = {
    def toOutput(value: Any): Option[Output] = {
      value match {
        case x: Error ⇒ Some(RpcError(error = x))
        case x: ErrorWithCommandId ⇒ Some(RpcError(error = x.error, id = x.commandId))
        case x: CommandResponse ⇒ Some(RpcResponse(result = x.result, id = x.id))
        case x: CommandRequest ⇒ Some(RpcRequest(id = x.id, method = x.method, params = x.params))
        case _ ⇒ None
      }
    }

    toOutput(value) map {
      x ⇒ TextMessage.Strict(write(x))
    }
  }
}
