package org.leveloneproject.central.kms.config


import com.google.inject.{Guice, Injector}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import slick.jdbc.PostgresProfile.backend.Database

class MainModuleSpec extends FlatSpec with MockitoSugar {

  trait Setup {
    val config: Config = ConfigFactory.load()
    val database: Database = mock[Database]

    trait FakeDatabaseCreator extends DatabaseCreator {
      override def createDatabase(config: Config): Database = database
    }

    val module = new MainModule(config) with FakeDatabaseCreator
    val injector: Injector = Guice.createInjector(module)
  }

  it should "bind Config to config" in new Setup {
    assert(injector.getInstance(classOf[Config]) == config)
  }
}
