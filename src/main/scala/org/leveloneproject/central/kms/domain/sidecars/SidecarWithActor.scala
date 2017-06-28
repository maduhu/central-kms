package org.leveloneproject.central.kms.domain.sidecars

import akka.actor.ActorRef

case class SidecarWithActor(sidecar: Sidecar, actor: ActorRef)
