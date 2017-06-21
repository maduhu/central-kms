package org.leveloneproject.central.kms.sidecar

import java.util.UUID

import akka.actor.ActorRef
import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.Error
import org.leveloneproject.central.kms.domain.batches.{Batch, BatchService}
import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheck, HealthCheckService}
import org.leveloneproject.central.kms.domain.sidecars.{RegisterResponse, Sidecar, SidecarService}
import org.leveloneproject.central.kms.sidecar.batch.BatchParameters
import org.leveloneproject.central.kms.sidecar.registration.RegisterParameters

import scala.concurrent.Future
import scala.language.implicitConversions

class SidecarSupport @Inject()(batchService: BatchService, sidecarService: SidecarService, healthCheckService: HealthCheckService) {
  def createBatch(sidecarId: UUID, batchParameters: BatchParameters): Future[Either[Error, Batch]] = {
    batchService.create(batchParameters.toCreateRequest(sidecarId))
  }

  def registerSidecar(registerParameters: RegisterParameters, actor: ActorRef): Future[Either[Error, RegisterResponse]] = {
    sidecarService.register(RegisterParameters.toRegisterRequest(registerParameters, actor))
  }

  def terminateSidecar(sidecar: Sidecar): Future[Sidecar] = {
    sidecarService.terminate(sidecar)
  }

  def completeHealthCheck(healthCheckId: UUID, response: String): Future[Either[Error, HealthCheck]] = {
    healthCheckService.complete(healthCheckId, response)
  }
}
