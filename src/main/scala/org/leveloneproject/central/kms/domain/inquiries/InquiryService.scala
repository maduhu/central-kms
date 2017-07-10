package org.leveloneproject.central.kms.domain.inquiries

import java.time.Instant
import java.time.temporal.ChronoUnit

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.sidecars.{SidecarAndActor, SidecarList}
import org.leveloneproject.central.kms.persistance.InquiriesRepository
import org.leveloneproject.central.kms.util.{FutureEither, IdGenerator, InstantProvider}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InquiryService @Inject()(sidecarList: SidecarList, inquiriesRepository: InquiriesRepository) extends IdGenerator with InstantProvider {

  def create(request: CreateInquiryRequest): Future[Either[KmsError, Inquiry]] = {
    for {
      sidecarAndActor ← FutureEither(validateRequest(request))
      inquiry ← createAndSave(request, sidecarAndActor)
    } yield inquiry
  }

  private def createAndSave(request: CreateInquiryRequest, sidecarAndActor: SidecarAndActor): FutureEither[KmsError, Inquiry] = {
    inquiriesRepository.insert(Inquiry(newId(), request.service, request.startTime, request.endTime, now(), InquiryStatus.Created, sidecarAndActor.id)) map { inquiry ⇒
      sidecarAndActor.actor ! inquiry
      Right(inquiry)
    }
  }

  private def validateRequest(request: CreateInquiryRequest): Future[Either[KmsError, SidecarAndActor]] = Future {
    for {
      _ ← validateDateRange(request.startTime, request.endTime)
      service ← validateServiceName(request.service)
    } yield service
  }

  private def validateDateRange(startTime: Instant, endTime: Instant): Either[KmsError, Unit] = {
    if (startTime.isAfter(endTime) | startTime.plus(30, ChronoUnit.DAYS).isBefore(endTime))
      Left(KmsError.invalidDateRange)
    else
      Right(Nil)
  }

  private def validateServiceName(service: String): Either[KmsError, SidecarAndActor] = {
    sidecarList.byName(service) match {
      case Some(sidecarAndActor) ⇒ Right(sidecarAndActor)
      case None ⇒ Left(KmsError.unregisteredSidecar(service))
    }
  }
}
