package org.leveloneproject.central.kms.domain.keys

import akka.http.scaladsl.server.Route
import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys.KeyDomain._
import org.leveloneproject.central.kms.routing.Router

class KeyRouter @Inject()(keyService: KeyService) extends Router {
  def route: Route = {
    path("key") {
      (post & entity(as[RegistrationRequest])) { _ ⇒
        onSuccess(keyService.create(KeyRequest())) { response ⇒
          complete(RegistrationResponse(response.id.toString, response.privateKey))
        }
      }
    }
  }
}
