package org.leveloneproject.central.kms.domain.batches

import java.sql.SQLException

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.persistance.{BatchRepository, DatabaseHelper}
import org.leveloneproject.central.kms.util.InstantProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BatchService @Inject()(batchRepository: BatchRepository) extends DatabaseHelper with InstantProvider {
  def create(request: CreateBatchRequest): Future[Either[KmsError, Batch]] = {
    val batch = Batch(request.batchId, request.sidecarId, request.signature, now())
    batchRepository.insert(batch) map { _ ⇒ Right(batch) } recover {
      case ex: SQLException if isPrimaryKeyViolation(ex) ⇒ Left(KmsError.batchExistsError(request.batchId))
      case _ ⇒ Left(KmsError.internalError)
    }
  }
}
