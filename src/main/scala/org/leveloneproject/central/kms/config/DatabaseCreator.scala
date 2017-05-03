package org.leveloneproject.central.kms.config

import com.typesafe.config.Config
import slick.jdbc.PostgresProfile.backend.{Database, DatabaseFactoryDef}

trait DatabaseCreator {
  val databaseFactory: DatabaseFactoryDef = Database

  def createDatabase(config: Config): Database = {
    databaseFactory.forConfig("db", config)
  }
}
