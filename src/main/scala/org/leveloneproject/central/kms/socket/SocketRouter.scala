package org.leveloneproject.central.kms.socket

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import org.leveloneproject.central.kms.routing.Router

class SocketRouter extends Router {

  val echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(m) ⇒ TextMessage("ECHO: " + m)
    case _ ⇒ TextMessage("Unsupported message")
  }

  def route: Route = {
    path("sidecar") {
      get {
        handleWebSocketMessages(echoService)
      }
    }
  }
}

