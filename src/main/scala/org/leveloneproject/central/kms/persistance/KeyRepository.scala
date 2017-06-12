package org.leveloneproject.central.kms.persistance

import java.util.UUID

import org.leveloneproject.central.kms.domain.Key

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait KeyRepository extends KeysTable {
  this: DbProfile ⇒

  val dbProvider: DbProvider
  import profile.api._

  private lazy val db = dbProvider.db

  def insert(key: Key) : Future[Unit] = db.run { keys += key } map { _ ⇒ }

  def getById(id: UUID) : Future[Option[Key]] = db.run { keys.filter(_.id === id).result.headOption }
}


