package org.leveloneproject.central.kms.persistance

import com.typesafe.config.Config
import org.flywaydb.core.Flyway

trait DatabaseMigrator {

  val flyway = new Flyway()

  def migrate(config: Config): Unit = {
    flyway.setDataSource(config.getString("db.url"), config.getString("db.user"), config.getString("db.password"))

    try {
      flyway.migrate()
    }
    catch {
      case _: Exception ⇒
        flyway.repair()
        flyway.migrate()
    }
  }
}
