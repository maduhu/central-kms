package org.leveloneproject.central.kms.domain.keys

import java.security.PublicKey
import java.util.UUID

import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.keys.KeyDomain._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class KeyServiceSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult {

  trait Setup {
    val keyGenerator: KeyGenerator = mock[KeyGenerator]
    val store: KeyStore = mock[KeyStore]
    val verifier: Verifier = mock[Verifier]
    val keyService = new KeyService(keyGenerator, store, verifier)
    val keyId: UUID = UUID.randomUUID()
    val serviceName: String = UUID.randomUUID().toString
  }

  "create" should "generate and return key" in new Setup {
    val privateKey = "some private key"
    val key = Key(keyId, serviceName, "some public key")
    when(store.create(any[Key])).thenReturn(Future(Right(key)))
    when(keyGenerator.generate()).thenReturn(Future(PublicPrivateKeyPair("", privateKey)))

    await(keyService.create(KeyRequest(keyId, serviceName))) shouldBe Right(KeyResponse(keyId, serviceName, privateKey))
  }

  it should "save key to store" in new Setup {
    val publicKey = "some public key"
    val privateKey = "some private key"
    val key = Key(keyId, serviceName, publicKey)
    when(store.create(key)).thenReturn(Future(Right(key)))

    when(keyGenerator.generate()).thenReturn(Future(PublicPrivateKeyPair(publicKey, privateKey)))

    await(keyService.create(KeyRequest(keyId, serviceName)))

    verify(store).create(key)
  }

  it should "return createError from keystore" in new Setup {
    private val error = new CreateError { val message = "test"}
    when(keyGenerator.generate()).thenReturn(Future(PublicPrivateKeyPair("", "")))
    when(store.create(any())).thenReturn(Future(Left(error)))

    await(keyService.create(KeyRequest(UUID.randomUUID(), serviceName))) shouldBe Left(error)
  }


  "validate" should "return not found response if key not in store" in new Setup {
    when(store.getById(keyId)).thenReturn(Future(None))

    val result: Either[ValidateError, ValidateResponse] = await(keyService.validate(ValidateRequest(keyId, "", "")))
    assert(result == Left(ValidateErrors.KeyNotFound))
  }

  it should "return invalid signature response if signature not verified" in new Setup {
    private val publicKey = mock[PublicKey]
    private val key = mock[Key]
    when(key.cryptoKey).thenReturn(publicKey)
    when(store.getById(keyId)).thenReturn(Future(Some(key)))
    private val signature = "signature"
    private val message = "message"
    when(verifier.verify(publicKey, signature, message)).thenReturn(Left(ValidateErrors.InvalidSignature))

    assert(await(keyService.validate(ValidateRequest(keyId, signature, message))) == Left(ValidateErrors.InvalidSignature))
  }

  it should "return success if signature verified" in new Setup {
    private val publicKey = mock[PublicKey]
    private val key = mock[Key]
    when(key.cryptoKey).thenReturn(publicKey)
    when(store.getById(keyId)).thenReturn(Future(Some(key)))
    private val signature = "signature"
    private val message = "message"
    when(verifier.verify(publicKey, signature, message)).thenReturn(Right(ValidateResponses.Success))

    assert(await(keyService.validate(ValidateRequest(keyId, signature, message))) == Right(ValidateResponses.Success))
  }

}
