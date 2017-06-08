package org.leveloneproject.central.kms.sidecar

import org.leveloneproject.central.kms.domain.keys.CreateError
import org.leveloneproject.central.kms.socket.RpcErrors

trait CreateErrorConverter {
  def toRegistrationError(commandId: String, error: CreateError): Any = {
    error match {
      case x: CreateError.KeyExists ⇒ SidecarErrors.DuplicateSidecarRegistered(commandId, x.message)
      case _ ⇒ RpcErrors.InternalError
    }
  }
}
