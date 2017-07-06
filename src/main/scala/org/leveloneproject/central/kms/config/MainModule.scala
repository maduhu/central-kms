package org.leveloneproject.central.kms.config

import java.security.Security
import java.time.Clock

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.Singleton
import com.typesafe.config.Config
import com.tzavellas.sse.guice.ScalaModule
import net.codingwell.scalaguice.ScalaMultibinder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.flywaydb.core.Flyway
import org.leveloneproject.central.kms.Service
import org.leveloneproject.central.kms.crypto._
import org.leveloneproject.central.kms.domain.healthchecks.HealthCheckRouter
import org.leveloneproject.central.kms.domain.sidecars.{SidecarList, SidecarRouter}
import org.leveloneproject.central.kms.persistance._
import org.leveloneproject.central.kms.persistance.postgres._
import org.leveloneproject.central.kms.routing.{RouteAggregator, Router}
import org.leveloneproject.central.kms.socket.{SocketRouter, WebSocketService}


class MainModule(config: Config) extends ScalaModule {
  implicit val system = ActorSystem("kms", config)

  def configure(): Unit = {

    Security.addProvider(new BouncyCastleProvider)

    implicit val materializer = ActorMaterializer()

    val asymmetric = new TweetNaClKeys()
    val symmetric = new CmacKeys
    bind[Config].toInstance(config)
    bind[ActorSystem].toInstance(system)
    bind[ActorMaterializer].toInstance(materializer)
    bind[SidecarList].in[Singleton]
    bindDatabase
    bind[Clock].toInstance(Clock.systemUTC())
    bind[KeyStore]
    bind[Flyway]
    bind[Migrator]
    bind[Service]
    bind[AsymmetricKeyGenerator].toInstance(asymmetric)
    bind[SymmetricKeyGenerator].toInstance(symmetric)
    bind[AsymmetricVerifier].toInstance(asymmetric)
    bind[SymmetricVerifier].toInstance(symmetric)
    bind[RouteAggregator]
    bind[WebSocketService]

    bindRouters()
  }


  private def bindDatabase = {
    bind[DbProvider].to[PostgresDbProvider].in[Singleton]
    bind[BatchRepository].to[PostgresBatchRepository]
    bind[KeyRepository].to[PostgresKeyRepository]
    bind[SidecarRepository].to[PostgresSidecarRepository]
    bind[HealthCheckRepository].to[PostgresHealthCheckRepository]
    bind[SidecarLogsRepository].to[PostgresSidecarLogsRepository]
  }

  private def bindRouters(): Unit = {
    val routerBinder = ScalaMultibinder.newSetBinder[Router](binder)
    routerBinder.addBinding.to[SocketRouter]
    routerBinder.addBinding.to[SidecarRouter]
    routerBinder.addBinding.to[HealthCheckRouter]
  }

}


