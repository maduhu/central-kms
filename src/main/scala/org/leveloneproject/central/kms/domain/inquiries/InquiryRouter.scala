package org.leveloneproject.central.kms.domain.inquiries

import java.time.Instant

import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Route
import com.google.inject.Inject
import org.leveloneproject.central.kms.routing.Router

class InquiryRouter @Inject()(creator: InquiryCreator) extends Router {
  def route: Route = pathPrefix("inquiry") {
    (post & entity(as[CreateRequest])) { req ⇒
      onSuccess(creator.create(CreateInquiryRequest(req.service, req.startTime, req.endTime))) {
        case Right(inquiry) ⇒
          respondWithHeader(Location(Uri(s"/inquiry/${inquiry.id}"))) & complete(StatusCodes.Created → None)
        case Left(error) ⇒ complete(StatusCodes.BadRequest → error)
      }
    }
  }
}

case class CreateRequest(service: String, startTime: Instant, endTime: Instant)
