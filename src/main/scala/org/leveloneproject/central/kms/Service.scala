package org.leveloneproject.central.kms

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.Inject

case class Service @Inject()(system: ActorSystem, materializer: ActorMaterializer, router: MainRouter)
