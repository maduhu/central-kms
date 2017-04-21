package org.leveloneproject.central.kms.persistance

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys.KeyDomain.Key
import org.leveloneproject.central.kms.persistance.dao.KeysTable
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait KeyStore {
  def create(key: Key): Future[Key]
}

class PostgresKeyStore @Inject()(database: Database) extends KeyStore with KeysTable {
  def create(key: Key): Future[Key] = {
    database.run(keys += key).map(_ ⇒ key)
  }
}
