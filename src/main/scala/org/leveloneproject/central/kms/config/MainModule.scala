package org.leveloneproject.central.kms.config

import java.security.KeyPairGeneratorSpi
import java.time.Clock

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import com.tzavellas.sse.guice.ScalaModule
import net.codingwell.scalaguice.ScalaMultibinder
import net.i2p.crypto.eddsa.KeyPairGenerator
import org.flywaydb.core.Flyway
import org.leveloneproject.central.kms.Service
import org.leveloneproject.central.kms.domain.keys.KeyGenerator
import org.leveloneproject.central.kms.persistance.postgres.{PostgresBatchRepository, PostgresDbProvider, PostgresKeyRepository}
import org.leveloneproject.central.kms.persistance._
import org.leveloneproject.central.kms.routing.{RouteAggregator, Router}
import org.leveloneproject.central.kms.socket.SocketRouter

class MainModule(config: Config) extends ScalaModule {

  def configure(): Unit = {
    implicit val system = ActorSystem("kms", config)
    implicit val materializer = ActorMaterializer()

    bind[Config].toInstance(config)
    bind[ActorSystem].toInstance(system)
    bind[ActorMaterializer].toInstance(materializer)
    bind[KeyPairGeneratorSpi].to[KeyPairGenerator]
    bind[DbProvider].to[PostgresDbProvider]
    bind[BatchRepository].to[PostgresBatchRepository]
    bind[KeyRepository].to[PostgresKeyRepository]
    bind[Clock].toInstance(Clock.systemUTC())
    bind[KeyStore]
    bind[Flyway]
    bind[Migrator]
    bind[Service]
    bind[KeyGenerator]
    bind[RouteAggregator]

    bindRouters()
  }

  def bindRouters(): Unit = {
    val routerBinder = ScalaMultibinder.newSetBinder[Router](binder)
    routerBinder.addBinding.to[SocketRouter]
  }

}


