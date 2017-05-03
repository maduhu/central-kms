package org.leveloneproject.central.kms.persistance

import com.typesafe.config.Config
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class MigratorSpec extends Specification with Mockito {
  private val url = "url"
  private val user = "user"
  private val password = "password"

  trait Setup extends Scope {
    val config: Config = mock[Config]
    val flyway: Flyway = mock[Flyway]

    config.getString("db.url") returns url
    config.getString("db.user") returns user
    config.getString("db.password") returns password

    val migrator = new Migrator(config, flyway)
  }

  "migrate" should {
    "setDataSource from config and migrate" in new Setup {

      migrator.migrate()

      there was one(flyway).setDataSource(url, user, password)
      there was one(flyway).migrate()
    }

    "repair migration on exception" in new Setup {
      flyway.migrate().throws(new FlywayException()).thenReturn(0)

      migrator.migrate()

      there was one(flyway).repair()
      there was two(flyway).migrate()
    }
  }
}
