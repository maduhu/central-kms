package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.crypto._
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.persistance.KeyStore
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class KeyServiceSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult {

  trait Setup {
    val asymmetricKeyGenerator: AsymmetricKeyGenerator = mock[AsymmetricKeyGenerator]
    val symmetricKeyGenerator: SymmetricKeyGenerator = mock[SymmetricKeyGenerator]
    val store: KeyStore = mock[KeyStore]
    val verifier: AsymmetricVerifier = mock[AsymmetricVerifier]
    val keyService = new KeyService(asymmetricKeyGenerator, symmetricKeyGenerator, store, verifier)
    val keyId: UUID = UUID.randomUUID()
    val serviceName: String = UUID.randomUUID().toString

    final val publicKey = "some public key"
    final val privateKey = "some private key"
    final val symmetricKey = "some symmetric key"

    def setupKeys(): Unit = {
      when(asymmetricKeyGenerator.generate()).thenReturn(Future(PublicPrivateKeyPair(publicKey, privateKey)))
      when(symmetricKeyGenerator.generate()).thenReturn(Future(symmetricKey))
    }
  }

  "create" should "generate and return key" in new Setup {
    val key = Key(keyId, "some public key")
    when(store.create(any[Key])).thenReturn(Future(Right(key)))
    setupKeys()

    await(keyService.create(CreateKeyRequest(keyId))) shouldBe Right(CreateKeyResponse(keyId, publicKey, privateKey, symmetricKey))
  }

  it should "save key to store" in new Setup {
    val key = Key(keyId, publicKey)
    when(store.create(key)).thenReturn(Future(Right(key)))

    setupKeys()

    await(keyService.create(CreateKeyRequest(keyId)))

    verify(store).create(key)
  }

  it should "return createError from keystore" in new Setup {
    private val error = KmsError(500, "any message")
    setupKeys()
    when(store.create(any())).thenReturn(Future(Left(error)))

    await(keyService.create(CreateKeyRequest(UUID.randomUUID()))) shouldBe Left(error)
  }


  "validate" should "return not found response if key not in store" in new Setup {
    when(store.getById(keyId)).thenReturn(Future(None))

    val result: Either[VerificationError, VerificationResult] = await(keyService.validate(ValidateRequest(keyId, "", "")))
    assert(result == Left(VerificationError.KeyNotFound))
  }

  it should "return invalid signature response if signature not verified" in new Setup {
    private val pubKey = "public key"
    private val key = mock[Key]
    when(key.publicKey).thenReturn(pubKey)
    when(store.getById(keyId)).thenReturn(Future(Some(key)))
    private val signature = "signature"
    private val message = "message"
    when(verifier.verify(pubKey, signature, message)).thenReturn(Left(VerificationError.InvalidSignature))

    assert(await(keyService.validate(ValidateRequest(keyId, signature, message))) == Left(VerificationError.InvalidSignature))
  }

  it should "return success if signature verified" in new Setup {
    private val pubKey = "public key"
    private val key = mock[Key]
    when(key.publicKey).thenReturn(pubKey)
    when(store.getById(keyId)).thenReturn(Future(Some(key)))
    private val signature = "signature"
    private val message = "message"
    when(verifier.verify(pubKey, signature, message)).thenReturn(Right(VerificationResult.Success))

    assert(await(keyService.validate(ValidateRequest(keyId, signature, message))) == Right(VerificationResult.Success))
  }

}
