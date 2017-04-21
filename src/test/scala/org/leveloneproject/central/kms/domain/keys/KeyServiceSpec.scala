package org.leveloneproject.central.kms.domain.keys

import java.security.PublicKey
import java.util.UUID

import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.keys.KeyDomain._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.concurrent.Future

class KeyServiceSpec extends Specification with Mockito with AwaitResult {

  trait Setup extends Scope {
    val keyGenerator: KeyGenerator = mock[KeyGenerator]
    val store: KeyStore = mock[KeyStore]
    val verifier: Verifier = mock[Verifier]
    val keyService = new KeyService(keyGenerator, store, verifier)
    val keyId: UUID = UUID.randomUUID()
  }

  "create" should {
    "generate and return key" in new Setup {
      val privateKey = "some private key"
      val key = Key(keyId, "some public key")
      store.create(any[Key]) returns Future(key)
      keyGenerator.generate returns Future(PublicPrivateKeyPair(keyId, "", privateKey))

      val result: KeyResponse = await(keyService.create(KeyRequest()))
      result.id must_== keyId
      result.privateKey must_== privateKey
    }

    "save key to store" in new Setup {
      val publicKey = "some public key"
      val privateKey = "some private key"
      val key = Key(keyId, publicKey)
      store.create(key) returns Future(key)

      keyGenerator.generate returns Future(PublicPrivateKeyPair(keyId, publicKey, privateKey))

      await(keyService.create(KeyRequest()))

      there was one(store).create(key)
    }
  }

  "validate" should {
    "return not found response if key not in store" in new Setup {
      store.getById(keyId) returns Future(None)

      val result: Either[ValidateError, ValidateResponse] = await(keyService.validate(ValidateRequest(keyId, "", "")))
      result must beLeft(ValidateErrors.KeyNotFound)
    }

    "return invalid signature response if signature not verified" in new Setup {
      private val publicKey = mock[PublicKey]
      private val key = mock[Key]
      key.cryptoKey returns publicKey
      store.getById(keyId) returns Future(Some(key))
      private val signature = "signature"
      private val message = "message"
      verifier.verify(publicKey, signature, message) returns Left(ValidateErrors.InvalidSignature)

      await(keyService.validate(ValidateRequest(keyId, signature, message))) must beLeft(ValidateErrors.InvalidSignature)
    }

    "return success if signature verified" in new Setup {
      private val publicKey = mock[PublicKey]
      private val key = mock[Key]
      key.cryptoKey returns publicKey
      store.getById(keyId) returns Future(Some(key))
      private val signature = "signature"
      private val message = "message"
      verifier.verify(publicKey, signature, message) returns Right(ValidateResponses.Success)

      await(keyService.validate(ValidateRequest(keyId, signature, message))) must beRight(ValidateResponses.Success)
    }
  }
}
