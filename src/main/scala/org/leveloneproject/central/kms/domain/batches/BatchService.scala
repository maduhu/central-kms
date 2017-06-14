package org.leveloneproject.central.kms.domain.batches

import java.sql.SQLException
import java.time.Clock

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.persistance.{BatchRepository, DatabaseHelper}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BatchService @Inject()(batchRepository: BatchRepository, clock: Clock) extends DatabaseHelper {
  def create(request: CreateBatchRequest): Future[Either[Error, Batch]] = {
    val batch = Batch(request.batchId, request.sidecarId, request.signature, clock.instant)
    batchRepository.insert(batch) map { _ ⇒ Right(batch) } recover {
      case ex: SQLException if isPrimaryKeyViolation(ex) ⇒ Left(Errors.BatchExistsError(request.batchId))
      case _ ⇒ Left(Errors.InternalError)
    }
  }
}
