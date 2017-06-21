package org.leveloneproject.central.kms.sidecar

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import org.json4s.JsonAST.JValue
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.domain.healthchecks.HealthCheck
import org.leveloneproject.central.kms.domain.sidecars.Sidecar
import org.leveloneproject.central.kms.routing.JsonSupport
import org.leveloneproject.central.kms.sidecar.SidecarActor.SidecarWithOutSocket
import org.leveloneproject.central.kms.sidecar.batch._
import org.leveloneproject.central.kms.sidecar.healthcheck.HealthCheckRequest
import org.leveloneproject.central.kms.sidecar.registration._
import org.leveloneproject.central.kms.util.FutureEither

class SidecarActor(sidecarSupport: SidecarSupport) extends Actor with JsonSupport {

  import context._

  val requests = collection.mutable.HashMap.empty[String, (String, JValue) ⇒ Unit]

  def connected(out: ActorRef): Receive = {
    case RegisterCommand(id, registerParameters) ⇒
      FutureEither(sidecarSupport.registerSidecar(registerParameters, self)) map { r ⇒
        val sidecar = r.sidecar
        val keyResponse = r.keyResponse
        become(registered(SidecarWithOutSocket(sidecar, out)))
        out ! Registered(id, RegisteredResult(sidecar.id, keyResponse.privateKey, ""))
      } recover {
        case x: Error ⇒ out ! ErrorWithCommandId(x, id)
      }
    case x: SideCarCommand ⇒ out ! Errors.MethodNotAllowedInCurrentState(x)
    case SidecarActor.Disconnect ⇒ terminate()
    case x ⇒ out ! x
  }

  def registered(sidecarWithActor: SidecarWithOutSocket): Receive = {
    case r: CompleteRequest ⇒ handleRequest(r)
    case BatchCommand(id, batchParameters) ⇒
      sidecarSupport.createBatch(sidecarWithActor.sidecarId, batchParameters).map {
        _.fold(e ⇒ ErrorWithCommandId(e, id), batch ⇒ BatchCreated(id, BatchCreatedResult(batch.id)))
      }.map { result ⇒ sidecarWithActor.socket ! result }
    case HealthCheck(id, _, level, _, _, _, _) ⇒
      requests += id.toString → completeHealthCheck
      sidecarWithActor.socket ! HealthCheckRequest(id, level)
    case x: SideCarCommand ⇒ sidecarWithActor.socket ! Errors.MethodNotAllowedInCurrentState(x)
    case SidecarActor.Disconnect ⇒ sidecarSupport.terminateSidecar(sidecarWithActor.sidecar) map { _ ⇒ terminate() }
    case x ⇒ sidecarWithActor.socket ! x
  }

  def receive: Receive = {
    case SidecarActor.Connected(out) ⇒ become(connected(out))
  }

  private def terminate(): Unit = {
    requests.clear()
    stop(self)
  }

  private def handleRequest(request: CompleteRequest): Unit = {
    requests.remove(request.id).foreach((req: (String, JValue) ⇒ Unit) ⇒ {
      (request.result, request.error) match {
        case (Some(result), _) ⇒ req(request.id, result)
        case _ ⇒ //ignore
      }
    })
  }

  private def completeHealthCheck(healthCheckId: String, result: JValue): Unit = {
    sidecarSupport.completeHealthCheck(UUID.fromString(healthCheckId), write(result))
  }
}

object SidecarActor {
  def props(sidecarSupport: SidecarSupport) = Props(new SidecarActor(sidecarSupport))

  case class Connected(outgoing: ActorRef)

  case class Disconnect()

  case class SidecarWithOutSocket(sidecar: Sidecar, socket: ActorRef) {
    val sidecarId: UUID = sidecar.id
  }

}
