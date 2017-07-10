package org.leveloneproject.central.kms.domain.healthchecks

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.domain.healthchecks.HealthCheckStatus.Pending
import org.leveloneproject.central.kms.domain.sidecars.SidecarList
import org.leveloneproject.central.kms.persistance.HealthCheckRepository
import org.leveloneproject.central.kms.util.{IdGenerator, InstantProvider}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HealthCheckService @Inject()(healthCheckRepository: HealthCheckRepository, sidecarList: SidecarList) extends IdGenerator with InstantProvider {

  def create(request: CreateHealthCheckRequest): Future[Either[KmsError, HealthCheck]] = {
    val id = request.sidecarId
    sidecarList.actorById(id) match {
      case Some(ref) ⇒
        healthCheckRepository.insert(HealthCheck(newId(), id, request.level, now(), Pending)) map { check ⇒
          ref ! check
          Right(check)
        }
      case None ⇒ Future.successful(Left(KmsError.unregisteredSidecar(id)))
    }
  }

  def complete(healthCheckId: UUID, response: String): Future[Either[KmsError, HealthCheck]] = {
    healthCheckRepository.complete(healthCheckId, response, now()).map {
      case None ⇒ Left(KmsError.healthCheckDoesNotExist)
      case Some(x) ⇒ Right(x)
    }
  }
}
