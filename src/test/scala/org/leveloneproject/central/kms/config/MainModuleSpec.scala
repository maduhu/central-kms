package org.leveloneproject.central.kms.config

import com.google.inject.Guice
import com.typesafe.config.{Config, ConfigFactory}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import slick.jdbc.PostgresProfile.backend.Database

class MainModuleSpec extends Specification with Mockito {

  trait Setup extends Scope {
    val config: Config = ConfigFactory.load()
    val database: Database = mock[Database]

    trait FakeDatabaseCreator extends DatabaseCreator {
      override def createDatabase(config: Config): Database = database
    }

    val module = new MainModule(config) with FakeDatabaseCreator
    val injector = Guice.createInjector(module)
  }

  "configure" should {

    "bind Config to config" in new Setup {
      injector.getInstance(classOf[Config]) must_== config
    }
  }
}
