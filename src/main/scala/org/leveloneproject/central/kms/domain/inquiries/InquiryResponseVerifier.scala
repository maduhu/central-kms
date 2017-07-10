package org.leveloneproject.central.kms.domain.inquiries

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.crypto.VerificationResult
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.batches.BatchFinder
import org.leveloneproject.central.kms.domain.keys.KeyVerifier
import org.leveloneproject.central.kms.util.{FutureEither, IdGenerator, InstantProvider}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


case class InquiryResponseRequest(inquiryId: UUID, batchId: UUID, body: String, total: Int, item: Int, sidecarId: UUID)

trait InquiryResponseVerifier {
  def verify(response: InquiryResponseRequest): Future[InquiryResponse]
}

class InquiryResponseVerifierImpl @Inject()(
                                             inquiriesStore: InquiriesStore,
                                             batchFinder: BatchFinder,
                                             keyVerifier: KeyVerifier,
                                             responseStore: InquiryResponsesStore)
  extends InquiryResponseVerifier with IdGenerator with InstantProvider {

  def verify(request: InquiryResponseRequest): Future[InquiryResponse] = {

    def updateInquiryStats(i: Option[Inquiry]): Future[Option[Inquiry]] = {
      i match {
        case Some(inquiry) ⇒
          val newCount = inquiry.responseCount + 1
          val newTotal = math.max(inquiry.total, request.total)
          val status = if (newCount == newTotal) InquiryStatus.Complete else InquiryStatus.Pending
          inquiriesStore.updateStats(inquiry.copy(status = status, total = newTotal, responseCount = newCount))
        case None ⇒ Future(None)
      }
    }

    def findAndUpdateInquiry(id: UUID): FutureEither[KmsError, Inquiry] =
      for {
        inquiry ← inquiriesStore.findById(id)
        updatedInquiry ← updateInquiryStats(inquiry)
      } yield updatedInquiry.toRight(KmsError.notFound("Inquiry", id))

    val response = InquiryResponse(newId(), request.inquiryId, request.batchId, request.body, request.item, now(), request.sidecarId)

    val result: Future[Either[KmsError, VerificationResult]] = for {
      _ ← findAndUpdateInquiry(request.inquiryId)
      batch ← FutureEither(batchFinder.findById(request.batchId))
      verificationResult ← FutureEither(keyVerifier.verify(batch.sidecarId, batch.signature, request.body))
    } yield verificationResult

    result
      .flatMap(e ⇒
        responseStore.create(e.fold(k ⇒ response.copy(verified = false, errorMessage = Some(k.message)), r ⇒ response.copy(verified = r.success, errorMessage = r.message))))
  }

}
