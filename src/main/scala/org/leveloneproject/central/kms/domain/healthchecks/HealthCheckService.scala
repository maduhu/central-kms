package org.leveloneproject.central.kms.domain.healthchecks

import java.time.Clock
import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.domain.healthchecks.HealthCheckStatus.Pending
import org.leveloneproject.central.kms.domain.sidecars.SidecarList
import org.leveloneproject.central.kms.persistance.HealthCheckRepository
import org.leveloneproject.central.kms.util.{FutureEither, IdGenerator}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HealthCheckService @Inject()(
                                    healthCheckRepository: HealthCheckRepository,
                                    clock: Clock,
                                    sidecarList: SidecarList) extends IdGenerator {

  def create(request: CreateHealthCheckRequest): Future[Either[Error, HealthCheck]] = {
    FutureEither(sidecarList.actorById(request.sidecarId)) flatMap { sidecar ⇒
      val healthCheck = HealthCheck(newId(), request.sidecarId, request.level, clock.instant(), Pending, None, None)
      healthCheckRepository.insert(healthCheck) map { _ ⇒
        sidecar ! healthCheck
        Right(healthCheck)
      }
    }
  }

  def complete(healthCheckId: UUID, response: String): Future[Either[Error, HealthCheck]] = {
    healthCheckRepository.complete(healthCheckId, response, clock.instant()).map {
      case None ⇒ Left(Errors.HealthCheckDoesNotExist)
      case Some(x) ⇒ Right(x)
    }
  }
}
