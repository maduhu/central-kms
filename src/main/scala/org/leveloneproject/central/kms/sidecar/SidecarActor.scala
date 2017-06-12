package org.leveloneproject.central.kms.sidecar

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import org.leveloneproject.central.kms.domain.{ErrorWithCommandId, Errors}
import org.leveloneproject.central.kms.domain.batches.BatchService
import org.leveloneproject.central.kms.domain.keys.KeyService
import org.leveloneproject.central.kms.sidecar.batch._
import org.leveloneproject.central.kms.sidecar.registration._

class SidecarActor(keyService: KeyService, batchService: BatchService) extends Actor {

  import context._

  def connected(out: ActorRef): Receive = {
    case RegisterCommand(id, registerParameters) ⇒
      keyService.create(registerParameters.toKeyRequest()).map {
        _.fold(
          e ⇒ ErrorWithCommandId(e, id),
          key ⇒ {
            become(registered(key.id, out))
            Registered(id, RegisteredResult(key.id, key.privateKey, ""))
          }
        )
      }.map { result ⇒ out ! result }
    case x: SideCarCommand ⇒ out ! Errors.MethodNotAllowedInCurrentState(x)
    case x ⇒ out ! x
  }

  def registered(sidecarId: UUID, out: ActorRef): Receive = {
    case BatchCommand(id, batchParameters) ⇒
      batchService.create(batchParameters.toCreateRequest(sidecarId)).map {
        _.fold(e ⇒ ErrorWithCommandId(e, id), batch ⇒ BatchCreated(id, BatchCreatedResult(batch.id)))
      }.map { result ⇒ out ! result }
    case x: SideCarCommand ⇒ out ! Errors.MethodNotAllowedInCurrentState(x)
    case x ⇒ out ! x
  }

  def receive: Receive = {
    case SidecarActor.Connected(out) ⇒ context.become(connected(out))
  }
}

object SidecarActor {
  def props(keyService: KeyService, batchService: BatchService) = Props(new SidecarActor(keyService, batchService))

  case class Connected(outgoing: ActorRef)
}
