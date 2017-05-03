package org.leveloneproject.central.kms.config

import java.security.KeyPairGeneratorSpi

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import com.tzavellas.sse.guice.ScalaModule
import net.codingwell.scalaguice.ScalaMultibinder
import net.i2p.crypto.eddsa.KeyPairGenerator
import org.flywaydb.core.Flyway
import org.leveloneproject.central.kms.Service
import org.leveloneproject.central.kms.domain.keys.{KeyGenerator, KeyRouter, KeyStore, ValidateRouter}
import org.leveloneproject.central.kms.persistance.Migrator
import org.leveloneproject.central.kms.persistance.postgres.PostgresKeyStore
import org.leveloneproject.central.kms.routing.{RouteAggregator, Router}
import slick.jdbc.PostgresProfile.api._

class MainModule(config: Config) extends ScalaModule with DatabaseCreator {

  def configure(): Unit = {
    implicit val system = ActorSystem("kms", config)
    implicit val materializer = ActorMaterializer()

    bind[Config].toInstance(config)
    bind[ActorSystem].toInstance(system)
    bind[ActorMaterializer].toInstance(materializer)
    bind[KeyPairGeneratorSpi].to[KeyPairGenerator]
    bind[Database].toInstance(createDatabase(config))
    bind[KeyStore].to[PostgresKeyStore]
    bind[Flyway]
    bind[Migrator]
    bind[Service]
    bind[KeyGenerator]
    bind[RouteAggregator]

    bindRouters()
  }

  def bindRouters() = {
    val routerBinder = ScalaMultibinder.newSetBinder[Router](binder)
    routerBinder.addBinding.to[KeyRouter]
    routerBinder.addBinding.to[ValidateRouter]
  }

}


