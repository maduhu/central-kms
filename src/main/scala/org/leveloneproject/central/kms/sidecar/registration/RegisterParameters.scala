package org.leveloneproject.central.kms.sidecar.registration

import java.util.UUID

import akka.actor.ActorRef
import org.leveloneproject.central.kms.domain.sidecars.RegisterRequest

import scala.language.implicitConversions

case class RegisterParameters(id: UUID, serviceName: String)

object RegisterParameters {
  implicit def toRegisterRequest(params: RegisterParameters)(implicit actor: ActorRef): RegisterRequest =
    RegisterRequest(params.id, params.serviceName, actor)
}
