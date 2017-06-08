package org.leveloneproject.central.kms.sidecar

import org.leveloneproject.central.kms.domain.{Error, ErrorWithCommandId}

object SidecarErrors {
  def DuplicateSidecarRegistered(commandId: String, message:String) = ErrorWithCommandId(Error(110, message), commandId)
  def MethodNotAllowedInCurrentState(command: SideCarCommand) = ErrorWithCommandId(Error(100, "'%s' method not allowed in current state".format(command.method)), command.commandId)
}


