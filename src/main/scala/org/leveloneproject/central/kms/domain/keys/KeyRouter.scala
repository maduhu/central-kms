package org.leveloneproject.central.kms.domain.keys

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.{Directives, Route}
import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys.KeyDomain._
import spray.json.DefaultJsonProtocol

class KeyRouter @Inject()(keyService: KeyService) extends Directives with SprayJsonSupport with DefaultJsonProtocol {

  val route: Route = path("key") {
    (post & entity(as[RegistrationRequest])) { _ ⇒
      onSuccess(keyService.create(KeyRequest())) { response ⇒
        complete(RegistrationResponse(response.id.toString, response.privateKey))
      }
    }
  }
}
