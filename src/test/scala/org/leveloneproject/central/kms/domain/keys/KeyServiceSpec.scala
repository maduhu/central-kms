package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.keys.KeyDomain.Key
import org.leveloneproject.central.kms.persistance.KeyStore
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.concurrent.Future

class KeyServiceSpec extends Specification with Mockito with AwaitResult {

  trait Setup extends Scope {
    val keyGenerator = mock[KeyGenerator]
    val store = mock[KeyStore]
    val keyService = new KeyService(keyGenerator, store)
  }

  "create" should {
    "generate and return key" in new Setup {
      val keyId = UUID.randomUUID()
      val privateKey = "some private key"
      val key = Key(keyId, "some public key")
      store.create(any[Key]) returns Future(key)
      keyGenerator.generate returns Future(PublicPrivateKeyPair(keyId, "", privateKey))

      val result = await(keyService.create(KeyRequest()))
      result.id must_== keyId
      result.privateKey must_== privateKey
    }

    "save key to store" in new Setup {
      val keyId = UUID.randomUUID()
      val publicKey = "some public key"
      val privateKey = "some private key"
      val key = Key(keyId, publicKey)
      store.create(key) returns Future(key)

      keyGenerator.generate returns Future(PublicPrivateKeyPair(keyId, publicKey, privateKey))

      await(keyService.create(KeyRequest()))

      there was one(store).create(key)
    }
  }
}
