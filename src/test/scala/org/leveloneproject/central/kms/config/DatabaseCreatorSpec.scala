package org.leveloneproject.central.kms.config

import com.typesafe.config.Config
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import slick.jdbc.PostgresProfile.backend.{Database, DatabaseFactoryDef}

class DatabaseCreatorSpec extends Specification with Mockito {

  trait Setup extends Scope {
    val dbFactory: DatabaseFactoryDef = mock[DatabaseFactoryDef]
    val config: Config = mock[Config]
    val database: Database = mock[Database]

    class TestDatabaseCreator extends DatabaseCreator {
      override val databaseFactory: DatabaseFactoryDef = dbFactory
    }

    val creator = new TestDatabaseCreator
  }

  "createDatabase" should {
    "use factory to create database from config" in new Setup {
      dbFactory.forConfig("db", config) returns database

      creator.createDatabase(config) must_== database
    }
  }
}
