package org.leveloneproject.central.kms.persistance.postgres

import java.util.UUID

import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.keys._
import org.mockito.Mockito._
import org.postgresql.util.PSQLException
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class PostgresKeyStoreSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult {

  trait Setup {
    val keysRepo: KeysRepo = mock[KeysRepo]
    val keyStore = new PostgresKeyStore(keysRepo)
  }

  "create" should "return DuplicateKey error when PrimaryKeyViolation thrown" in new Setup {
    private val exception = mock[PSQLException]
    when(exception.getSQLState).thenReturn("23505")
    private val keyId = UUID.randomUUID()
    val key = Key(keyId, "service name", "public key")
    when(keysRepo.insert(key)).thenReturn(Future.failed(exception))

    await(keyStore.create(key)) shouldBe Left(CreateError.KeyExists(keyId))
  }

  it should "return DatabaseFailed" in new Setup {
    private val exception = mock[PSQLException]
    private val keyId = UUID.randomUUID()
    val key = Key(keyId, "service name", "public key")
    when(keysRepo.insert(key)).thenReturn(Future.failed(exception))

    await(keyStore.create(key)) shouldBe Left(CreateError.DatabaseFailed())
  }

  it should "return CreateFailed when exception thrown" in new Setup {
    private val exception = mock[Exception]
    private val keyId = UUID.randomUUID()
    val key = Key(keyId, "service name", "public key")
    when(keysRepo.insert(key)).thenReturn(Future.failed(exception))

    await(keyStore.create(key)) shouldBe Left(CreateError.CreateFailed())
  }
}
