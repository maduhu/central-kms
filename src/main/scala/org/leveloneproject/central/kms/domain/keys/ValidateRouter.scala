package org.leveloneproject.central.kms.domain.keys

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys.KeyDomain._
import org.leveloneproject.central.kms.routing.Router

class ValidateRouter @Inject()(keyService: KeyService) extends Router {
  def route: Route = {
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
