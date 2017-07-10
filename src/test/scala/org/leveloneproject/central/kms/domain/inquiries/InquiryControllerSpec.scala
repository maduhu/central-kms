package org.leveloneproject.central.kms.domain.inquiries

import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.AwaitResult
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class InquiryControllerSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult{

  trait Setup {
    val inquiries: InquiriesStore = mock[InquiriesStore]
    val responses: InquiryResponsesStore = mock[InquiryResponsesStore]
    val controller = new InquiryController(inquiries, responses)
    val inquiryId: UUID = UUID.randomUUID()
    val now: Instant = Instant.now()

    def inquiryResponse(batchId: UUID = UUID.randomUUID(), verified: Boolean = true) =
      InquiryResponse(UUID.randomUUID(), inquiryId, batchId, "body", 1, now, UUID.randomUUID(), verified, None)
  }

  "getInquirySummaryById" should "return None if inquiry does not exists" in new Setup {
    when(inquiries.findById(inquiryId)).thenReturn(Future(None))

    await(controller.getInquirySummaryById(inquiryId)) shouldBe None

    verify(responses, times(0)).findByInquiryId(any())
  }

  it should "return summary with empty responses if no responses found" in new Setup {
    private val total = 100
    private val responseCount = 50
    private val status = InquiryStatus.Pending
    private val inquiry = Inquiry(inquiryId, "service", now, now, now, status, UUID.randomUUID, total, responseCount)
    when(inquiries.findById(inquiryId)).thenReturn(Future(Some(inquiry)))
    when(responses.findByInquiryId(inquiryId)).thenReturn(Future(Seq()))

    await(controller.getInquirySummaryById(inquiryId)) shouldBe Some(InquirySummary(status, total, responseCount, Seq.empty))
  }

  it should "return summary with responses" in new Setup {
    private val total = 100
    private val responseCount = 50
    private val status = InquiryStatus.Pending
    private val inquiry = Inquiry(inquiryId, "service", now, now, now, status, UUID.randomUUID, total, responseCount)
    when(inquiries.findById(inquiryId)).thenReturn(Future(Some(inquiry)))
    private val response1Id = UUID.randomUUID()
    private val response2Id = UUID.randomUUID()
    private val r = Seq(inquiryResponse(response1Id), inquiryResponse(response2Id, verified = false))
    when(responses.findByInquiryId(inquiryId)).thenReturn(Future(r))

    private val expectedSummaries = Seq(InquiryResponseSummary(response1Id, "body", verified = true), InquiryResponseSummary(response2Id, "body", verified = false))
    await(controller.getInquirySummaryById(inquiryId)) shouldBe Some(InquirySummary(status, total, responseCount, expectedSummaries))
  }
}
