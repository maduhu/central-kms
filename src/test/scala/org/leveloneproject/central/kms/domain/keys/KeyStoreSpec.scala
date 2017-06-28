package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.persistance.{KeyRepository, KeyStore}
import org.mockito.Mockito._
import org.postgresql.util.PSQLException
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class KeyStoreSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult {

  trait Setup {
    final val keysRepo: KeyRepository = mock[KeyRepository]
    final val keyStore = new KeyStore(keysRepo)
  }

  "create" should "return DuplicateKey error when PrimaryKeyViolation thrown" in new Setup {
    private val exception = mock[PSQLException]
    when(exception.getSQLState).thenReturn("23505")
    private val keyId = UUID.randomUUID()
    val key = Key(keyId, "public key")
    when(keysRepo.insert(key)).thenReturn(Future.failed(exception))

    await(keyStore.create(key)) shouldBe Left(KmsError.sidecarExistsError(keyId))
  }

  it should "return InternalError on exception" in new Setup {
    private val exception = mock[PSQLException]
    private val keyId = UUID.randomUUID()
    val key = Key(keyId, "public key")
    when(keysRepo.insert(key)).thenReturn(Future.failed(exception))

    await(keyStore.create(key)) shouldBe Left(KmsError.internalError)
  }
}
