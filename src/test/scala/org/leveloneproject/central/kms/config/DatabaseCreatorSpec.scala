package org.leveloneproject.central.kms.config

import com.typesafe.config.Config
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import slick.jdbc.PostgresProfile.backend.{Database, DatabaseFactoryDef}

class DatabaseCreatorSpec extends FlatSpec with MockitoSugar {

  trait Setup {
    val dbFactory: DatabaseFactoryDef = mock[DatabaseFactoryDef]
    val config: Config = mock[Config]
    val database: Database = mock[Database]

    class TestDatabaseCreator extends DatabaseCreator {
      override val databaseFactory: DatabaseFactoryDef = dbFactory
    }

    val creator = new TestDatabaseCreator
  }

  it should "use factory to create database from config" in new Setup {
    when(dbFactory.forConfig("db", config)).thenReturn(database)

    assert(creator.createDatabase(config) == database)
  }
}
