package org.leveloneproject.central.kms.socket

import akka.http.scaladsl.server.Route
import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.batches.BatchService
import org.leveloneproject.central.kms.domain.sidecars.SidecarService
import org.leveloneproject.central.kms.routing.Router

class SocketRouter @Inject()(batchService: BatchService, sidecarService: SidecarService) extends Router {

  def route: Route = {
    path("sidecar") {
      get {
        handleWebSocketMessages(new WebSocketService(batchService, sidecarService).sidecarFlow())
      }
    }
  }
}
