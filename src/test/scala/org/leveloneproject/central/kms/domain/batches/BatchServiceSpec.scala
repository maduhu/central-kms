package org.leveloneproject.central.kms.domain.batches

import java.sql.SQLException
import java.time.{Clock, Instant}
import java.util.UUID

import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.{Batch, Errors}
import org.leveloneproject.central.kms.persistance.BatchRepository
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class BatchServiceSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult {

  trait Setup {
    val repo: BatchRepository = mock[BatchRepository]
    val clock = mock[Clock]
    val service = new BatchService(repo, clock)
    val sidecarId: UUID = UUID.randomUUID()
    val batchId: UUID = UUID.randomUUID()
    val now = Instant.now()
    when(clock.instant()).thenReturn(now)
  }

  "create" should "save batch to repo" in new Setup {
    when(repo.insert(any())).thenReturn(Future.successful((): Unit))

    private val result = await(service.create(CreateRequest(sidecarId, batchId, "signature")))
    private val batch = Batch(batchId, sidecarId, "signature", now)
    result shouldBe Right(batch)

    verify(repo, times(1)).insert(batch)
  }

  it should "return BatchExistsError if duplicate key exception is thrown by repo" in new Setup {
    private val ex = mock[SQLException]
    when(ex.getSQLState).thenReturn("23505")

    when(repo.insert(any())).thenReturn(Future.failed(ex))

    await(service.create(CreateRequest(sidecarId, batchId, "signature"))) shouldBe Left(Errors.BatchExistsError(batchId))
  }

  it should "return InernalError if exception is thrown by repo" in new Setup {
    private val ex = mock[Exception]

    when(repo.insert(any())).thenReturn(Future.failed(ex))

    await(service.create(CreateRequest(sidecarId, batchId, "signature"))) shouldBe Left(Errors.InternalError)
  }


}
