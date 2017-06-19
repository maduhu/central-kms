package org.leveloneproject.central.kms.sidecar

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.domain.batches.BatchService
import org.leveloneproject.central.kms.domain.healthchecks.HealthCheck
import org.leveloneproject.central.kms.domain.sidecars.{Sidecar, SidecarService}
import org.leveloneproject.central.kms.sidecar.SidecarActor.SidecarWithOutSocket
import org.leveloneproject.central.kms.sidecar.batch._
import org.leveloneproject.central.kms.sidecar.healthcheck.HealthCheckRequest
import org.leveloneproject.central.kms.sidecar.registration._
import org.leveloneproject.central.kms.util.FutureEither

class SidecarActor(batchService: BatchService, sidecarService: SidecarService) extends Actor {

  import context._

  def connected(out: ActorRef): Receive = {
    case RegisterCommand(id, registerParameters) ⇒
      FutureEither(sidecarService.register(registerParameters)) map { r ⇒
        val sidecar = r.sidecar
        val keyResponse = r.keyResponse
        become(registered(SidecarWithOutSocket(sidecar, out)))
        out ! Registered(id, RegisteredResult(sidecar.id, keyResponse.privateKey, ""))
      } recover {
        case x: Error ⇒ out ! ErrorWithCommandId(x, id)
      }
    case x: SideCarCommand ⇒ out ! Errors.MethodNotAllowedInCurrentState(x)
    case SidecarActor.Disconnect ⇒ stop(self)
    case x ⇒ out ! x
  }

  def registered(sidecarWithActor: SidecarWithOutSocket): Receive = {
    case BatchCommand(id, batchParameters) ⇒
      batchService.create(batchParameters.toCreateRequest(sidecarWithActor.sidecarId)).map {
        _.fold(e ⇒ ErrorWithCommandId(e, id), batch ⇒ BatchCreated(id, BatchCreatedResult(batch.id)))
      }.map { result ⇒ sidecarWithActor.socket ! result }
    case HealthCheck(id, _, level, _, _, _, _) ⇒ sidecarWithActor.socket ! HealthCheckRequest(id, level)
    case x: SideCarCommand ⇒ sidecarWithActor.socket ! Errors.MethodNotAllowedInCurrentState(x)
    case SidecarActor.Disconnect ⇒ sidecarService.terminate(sidecarWithActor.sidecar) map { _ ⇒ stop(self) }
    case x ⇒ sidecarWithActor.socket ! x
  }

  def receive: Receive = {
    case SidecarActor.Connected(out) ⇒ become(connected(out))
  }
}

object SidecarActor {
  def props(batchService: BatchService, sidecarService: SidecarService) = Props(new SidecarActor(batchService, sidecarService))

  case class Connected(outgoing: ActorRef)
  case class Disconnect()
  case class SidecarWithOutSocket(sidecar: Sidecar, socket: ActorRef) {
    val sidecarId: UUID = sidecar.id
  }
}
