package org.leveloneproject.central.kms.sidecar

import akka.actor.{Actor, ActorRef, Props}
import org.leveloneproject.central.kms.domain.keys.KeyDomain.KeyRequest
import org.leveloneproject.central.kms.domain.keys.KeyService
import org.leveloneproject.central.kms.socket.RpcErrors

class SidecarActor(keyService: KeyService) extends Actor with CreateErrorConverter {

  import context._

  def connected(out: ActorRef): Receive = {
    case RegisterCommand(id, registerParameters) ⇒
      keyService.create(KeyRequest(registerParameters.id, registerParameters.serviceName))
        .map {
          _.fold(
            cr ⇒ out ! toRegistrationError(id, cr),
            key ⇒ {
              become(registered(out))
              out ! Registered(id, RegisteredResult(key.id, key.privateKey, ""))
            })
        }.recover {
        case _: Throwable ⇒
          out ! RpcErrors.InternalError
      }

    case x: SideCarCommand ⇒ out ! RpcErrors.MethodNotFound(x.commandId)
    case x ⇒ out ! x
  }

  def registered(out: ActorRef): Receive = {
    case command: SideCarCommand ⇒ out ! SidecarErrors.MethodNotAllowedInCurrentState(command)
    case _ ⇒ out ! _
  }

  def receive: Receive = {
    case SidecarActor.Connected(out) ⇒ context.become(connected(out))
  }
}

object SidecarActor {
  def props(keyService: KeyService) = Props(new SidecarActor(keyService))

  case class Connected(outgoing: ActorRef)

}
