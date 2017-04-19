package org.leveloneproject.central.kms

import akka.http.scaladsl.Http
import com.google.inject.Guice
import com.typesafe.config.ConfigFactory
import org.leveloneproject.central.kms.config.MainModule

import scala.concurrent.ExecutionContext.Implicits.global

object Boot extends App {
  val config = ConfigFactory.load()
  val injector = Guice.createInjector(new MainModule(config))

  var service = injector.getInstance(classOf[Service])
  implicit val system = service.system

  implicit val materializer = service.materializer

  Http().bindAndHandle(service.router.route, "0.0.0.0", 8080)
}

