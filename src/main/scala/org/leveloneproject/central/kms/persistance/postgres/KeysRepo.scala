package org.leveloneproject.central.kms.persistance.postgres

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys.Key
import org.leveloneproject.central.kms.persistance.dao.KeysTable
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KeysRepo @Inject()(database: Database) extends KeysTable {
  def insert(key: Key): Future[Unit] = database.run(keys += key).map { _ ⇒ }

  def getById(id: UUID): Future[Option[Key]] = database.run(keys.filter(_.id === id).result.headOption)
}
