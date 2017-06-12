package org.leveloneproject.central.kms.sidecar

trait SideCarCommand {
  val commandId: String
  val method: String
}
