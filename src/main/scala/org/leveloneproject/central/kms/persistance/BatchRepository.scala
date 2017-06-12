package org.leveloneproject.central.kms.persistance

import org.leveloneproject.central.kms.domain.Batch

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BatchRepository extends BatchesTable {
  this: DbProfile ⇒

  val dbProvider: DbProvider

  import profile.api._

  private lazy val db = dbProvider.db

  def insert(batch: Batch): Future[Unit] = db.run { batches += batch } map { _ ⇒ }
}
