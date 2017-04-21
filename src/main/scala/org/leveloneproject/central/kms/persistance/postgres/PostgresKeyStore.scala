package org.leveloneproject.central.kms.persistance.postgres

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys.KeyDomain.Key
import org.leveloneproject.central.kms.domain.keys.KeyStore
import org.leveloneproject.central.kms.persistance.dao.KeysTable
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostgresKeyStore @Inject()(database: Database) extends KeyStore with KeysTable {
  def create(key: Key): Future[Key] = {
    database.run(keys += key).map(_ ⇒ key)
  }

  def getById(id: UUID): Future[Option[Key]] = {
    database.run(keys.filter(_.id === id).result.headOption)
  }
}
