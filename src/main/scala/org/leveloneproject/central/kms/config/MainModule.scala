package org.leveloneproject.central.kms.config

import java.security.KeyPairGeneratorSpi

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import com.tzavellas.sse.guice.ScalaModule
import net.i2p.crypto.eddsa.KeyPairGenerator
import org.leveloneproject.central.kms.{MainRouter, Service}
import org.leveloneproject.central.kms.domain.keys.{KeyGenerator, KeyRouter}
import org.leveloneproject.central.kms.persistance.{KeyStore, PostgresKeyStore}
import slick.jdbc.PostgresProfile.api._

class MainModule(config: Config) extends ScalaModule {
  private final val AppName = "kms"

  def configure(): Unit = {
    implicit val system = ActorSystem(AppName, config)
    implicit val materializer = ActorMaterializer()

    bind[ActorSystem].toInstance(system)
    bind[ActorMaterializer].toInstance(materializer)
    bind[KeyPairGeneratorSpi].to[KeyPairGenerator]
    bind[Database].toInstance(Database.forConfig("db", config))
    bind[KeyStore].to[PostgresKeyStore]
    bind[Service]
    bind[KeyGenerator]
    bind[KeyRouter]
    bind[MainRouter]
  }
}
