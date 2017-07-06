package org.leveloneproject.central.kms.sidecar

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.batches.{Batch, BatchService, CreateBatchRequest}
import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheck, HealthCheckService}
import org.leveloneproject.central.kms.domain.sidecars._

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.concurrent.ExecutionContext.Implicits.global

class SidecarActions @Inject()(batchService: BatchService,
                               sidecarService: SidecarService,
                               healthCheckService: HealthCheckService,
                               challengeVerifier: ChallengeVerifier) {

  def createBatch(sidecar: Sidecar, params: SaveBatchParameters): Future[Either[KmsError, Batch]] = {
    batchService.create(CreateBatchRequest(sidecar.id, params.id, params.signature))
  }

  def registerSidecar(registerParameters: RegisterParameters): Future[Either[KmsError, RegisterResponse]] = {
    sidecarService.register(RegisterRequest(registerParameters.id, registerParameters.serviceName))
  }

  def challenge(sidecarWithActor: SidecarAndActor, keys: ChallengeKeys, answer: ChallengeAnswer): Future[Either[KmsError, SidecarAndActor]] = {
    challengeVerifier.verify(sidecarWithActor.sidecar.challenge, keys, answer) match {
      case Left(e) ⇒
        sidecarService.suspend(sidecarWithActor, e.message) map {
          case Left(suspendError) ⇒ Left(e)
          case Right(_) ⇒ Left(e)
        }
      case Right(_) ⇒ sidecarService.challengeAccepted(sidecarWithActor)
    }
  }

  def terminateSidecar(sidecar: Sidecar): Future[Either[KmsError, Sidecar]] = {
    sidecarService.terminate(sidecar)
  }

  def completeHealthCheck(healthCheckId: UUID, response: String): Future[Either[KmsError, HealthCheck]] = {
    healthCheckService.complete(healthCheckId, response)
  }
}
