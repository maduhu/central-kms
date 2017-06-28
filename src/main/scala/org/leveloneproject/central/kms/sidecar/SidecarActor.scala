package org.leveloneproject.central.kms.sidecar

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import org.leveloneproject.central.kms.domain.healthchecks.HealthCheck
import org.leveloneproject.central.kms.domain.sidecars.{RegisterResponse, SidecarWithActor}
import org.leveloneproject.central.kms.socket.{JsonRequest, JsonResponse}
import org.leveloneproject.central.kms.util.JsonSerializer

import scala.language.implicitConversions

class SidecarActor(sidecarSupport: SidecarSupport) extends Actor with JsonSerializer {

  import Responses._
  import context._

  val requests = collection.mutable.HashMap.empty[String, (String, AnyRef) ⇒ Unit]

  def connected(out: ActorRef): Receive = {
    case Register(id, registerParameters) ⇒
      sidecarSupport.registerSidecar(registerParameters, self) map {
        case Right(response) ⇒
          become(challenged(SidecarWithActor(response.sidecar, out)))
          out ! sidecarRegistered(id, response)
        case Left(error) ⇒ out ! commandError(id, error)
      }
    case command: Command ⇒ out ! methodNotAllowed(command)
    case Disconnect ⇒ terminate()
    case x ⇒ out ! x
  }

  def challenged(sidecarWithActor: SidecarWithActor): Receive = {
    case Challenge(id, params) ⇒
      become(registered(sidecarWithActor))
      sidecarWithActor.actor ! challengeAccepted(id)
    case command: Command ⇒ sidecarWithActor.actor ! methodNotAllowed(command)
    case Disconnect ⇒ terminate()
    case x ⇒ sidecarWithActor.actor ! x
  }

  def registered(sidecarWithActor: SidecarWithActor): Receive = {
    case CompleteRequest(jsonResponse) ⇒ handleRequest(jsonResponse)
    case SaveBatch(id, params) ⇒
      sidecarSupport.createBatch(sidecarWithActor.sidecar.id, params).map {
        _.fold(e ⇒ commandError(id, e), batch ⇒ batchCreated(id, batch))
      }.map { result ⇒ sidecarWithActor.actor ! result }
    case healthCheck: HealthCheck ⇒ request(sidecarWithActor.actor, healthCheckRequest(healthCheck), completeHealthCheck)
    case command: Command ⇒ sidecarWithActor.actor ! methodNotAllowed(command)
    case Disconnect ⇒ sidecarSupport.terminateSidecar(sidecarWithActor.sidecar) map { _ ⇒ terminate() }
    case x ⇒ sidecarWithActor.actor ! x
  }

  def receive: Receive = {
    case Connected(socket) ⇒ become(connected(socket))
  }

  private def request(out: ActorRef, request: JsonRequest, handler: (String, AnyRef) ⇒ Unit) = {
    requests += request.id → handler
    out ! request
  }

  private def terminate(): Unit = {
    requests.clear()
    stop(self)
  }

  private def handleRequest(request: JsonResponse): Unit = {
    requests.remove(request.id).foreach((req: (String, AnyRef) ⇒ Unit) ⇒ {
      (request.result, request.error) match {
        case (Some(result), _) ⇒ req(request.id, result)
        case _ ⇒ //ignore
      }
    })
  }

  private def completeHealthCheck(healthCheckId: String, result: AnyRef): Unit = {
    sidecarSupport.completeHealthCheck(UUID.fromString(healthCheckId), serialize(result))
  }

  private implicit def toRegisteredResult(response: RegisterResponse): RegisteredResult = RegisteredResult(response.sidecar.id, response.keyResponse.privateKey, response.keyResponse.symmetricKey, response.sidecar.challenge)
}

object SidecarActor {
  def props(sidecarSupport: SidecarSupport) = Props(new SidecarActor(sidecarSupport))

}
