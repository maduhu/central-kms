package org.leveloneproject.central.kms.domain.inquiries

import scala.concurrent.Future

trait InquiryResponsesStore {
  def create(response: InquiryResponse): Future[InquiryResponse]
}
