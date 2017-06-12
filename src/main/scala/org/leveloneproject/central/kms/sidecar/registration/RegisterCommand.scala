package org.leveloneproject.central.kms.sidecar.registration

import org.leveloneproject.central.kms.sidecar.SideCarCommand

case class RegisterCommand(commandId: String, params: RegisterParameters) extends SideCarCommand {
  val method = "register"
}
