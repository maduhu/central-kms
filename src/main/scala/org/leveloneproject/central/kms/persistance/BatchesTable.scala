package org.leveloneproject.central.kms.persistance

import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.domain.batches.Batch

trait BatchesTable extends DataMappers {
  this: DbProfile ⇒

  import profile.api._

  class BatchesTable(tag: Tag) extends Table[Batch](tag, "batches") {
    def id = column[UUID]("id")
    def sidecarId = column[UUID]("sidecar_id")
    def signature = column[String]("signature")
    def timestamp = column[Instant]("timestamp")
    def * = (id, sidecarId, signature, timestamp) <> (Batch.tupled, Batch.unapply)
  }

  val batches = TableQuery[BatchesTable]
}
