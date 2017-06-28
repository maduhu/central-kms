package org.leveloneproject.central.kms.sidecar

import java.util.UUID

import akka.actor.ActorRef
import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.batches.{Batch, BatchService, CreateBatchRequest}
import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheck, HealthCheckService}
import org.leveloneproject.central.kms.domain.sidecars.{RegisterRequest, RegisterResponse, Sidecar, SidecarService}

import scala.concurrent.Future
import scala.language.implicitConversions

class SidecarSupport @Inject()(batchService: BatchService, sidecarService: SidecarService, healthCheckService: HealthCheckService) {
  def createBatch(sidecarId: UUID, params: SaveBatchParameters): Future[Either[KmsError, Batch]] = {
    batchService.create(CreateBatchRequest(sidecarId, params.id, params.signature))
  }

  def registerSidecar(registerParameters: RegisterParameters, actor: ActorRef): Future[Either[KmsError, RegisterResponse]] = {
    sidecarService.register(RegisterRequest(registerParameters.id, registerParameters.serviceName, actor))
  }

  def terminateSidecar(sidecar: Sidecar): Future[Either[KmsError, Sidecar]] = {
    sidecarService.terminate(sidecar)
  }

  def completeHealthCheck(healthCheckId: UUID, response: String): Future[Either[KmsError, HealthCheck]] = {
    healthCheckService.complete(healthCheckId, response)
  }
}
