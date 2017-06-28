package org.leveloneproject.central.kms.sidecar

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.batches.{Batch, BatchService, CreateBatchRequest}
import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheck, HealthCheckService}
import org.leveloneproject.central.kms.domain.sidecars._

import scala.concurrent.Future
import scala.language.implicitConversions

class SidecarSupport @Inject()(batchService: BatchService, sidecarService: SidecarService, healthCheckService: HealthCheckService) {
  def createBatch(sidecar: Sidecar, params: SaveBatchParameters): Future[Either[KmsError, Batch]] = {
    batchService.create(CreateBatchRequest(sidecar.id, params.id, params.signature))
  }

  def registerSidecar(registerParameters: RegisterParameters): Future[Either[KmsError, RegisterResponse]] = {
    sidecarService.register(RegisterRequest(registerParameters.id, registerParameters.serviceName))
  }

  def challenge(sidecarWithActor: SidecarAndActor): Future[Either[KmsError, SidecarAndActor]] = {
    sidecarService.challengeAccepted(sidecarWithActor)
  }

  def terminateSidecar(sidecar: Sidecar): Future[Either[KmsError, Sidecar]] = {
    sidecarService.terminate(sidecar)
  }

  def completeHealthCheck(healthCheckId: UUID, response: String): Future[Either[KmsError, HealthCheck]] = {
    healthCheckService.complete(healthCheckId, response)
  }
}
