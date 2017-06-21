package org.leveloneproject.central.kms.sidecar

import org.json4s.JsonAST.JValue

trait SidecarMessage

trait SideCarCommand extends SidecarMessage {
  val commandId: String
  val method: String
}

case class CompleteRequest(id: String, result: Option[JValue], error: Option[JValue]) extends SidecarMessage
