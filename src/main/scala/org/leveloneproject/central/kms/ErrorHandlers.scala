package org.leveloneproject.central.kms

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, MalformedRequestContentRejection, RejectionHandler}
import org.leveloneproject.central.kms.domain.Error
import org.leveloneproject.central.kms.routing.JsonSupport

object ErrorHandlers extends JsonSupport {
  val rejectionHandler: RejectionHandler = RejectionHandler.newBuilder()
    .handle {
      case MalformedRequestContentRejection(message, _) ⇒
        complete(StatusCodes.BadRequest, Error(1, message))
    }
    .result()

  val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: Throwable ⇒
      complete(StatusCodes.InternalServerError, Error(500, e.getMessage))
  }

}
