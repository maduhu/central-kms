package org.leveloneproject.central.kms.domain.keys

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys.KeyDomain._
import spray.json.DefaultJsonProtocol

class KeyRouter @Inject()(keyService: KeyService) extends Directives with SprayJsonSupport with DefaultJsonProtocol {

  val route: Route = {
    path("key") {
      (post & entity(as[RegistrationRequest])) { _ ⇒
        onSuccess(keyService.create(KeyRequest())) { response ⇒
          complete(RegistrationResponse(response.id.toString, response.privateKey))
        }
      }
    } ~
    pathPrefix("validate") {
      pathPrefix(JavaUUID) { id ⇒
        pathEndOrSingleSlash {
          (put & entity(as[ValidationRequest])) { req ⇒
            onSuccess(keyService.validate(ValidateRequest(id, req.signature, req.message))) {
              case Left(r@ValidateErrors.KeyNotFound) ⇒ complete(StatusCodes.NotFound → r)
              case Left(e: ValidateError) ⇒ complete(StatusCodes.BadRequest → e)
              case Right(r) ⇒ complete(StatusCodes.OK → r)
            }
          }
        }
      }
    }
  }
}
