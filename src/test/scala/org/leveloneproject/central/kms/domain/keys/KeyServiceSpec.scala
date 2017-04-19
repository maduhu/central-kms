package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import org.leveloneproject.central.kms.AwaitResult
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.concurrent.Future

class KeyServiceSpec extends Specification with Mockito with AwaitResult {

  trait Setup extends Scope {
    val keyGenerator = mock[KeyGenerator]
    val keyService = new KeyService(keyGenerator)
  }

  "create" should {
    "generate and return key" in new Setup {
      val keyId = UUID.randomUUID()
      val privateKey = "some private key"
      keyGenerator.generate returns Future(PublicPrivateKeyPair(keyId, "", privateKey))

      val result = await(keyService.create(KeyRequest()))
      result.id must_== keyId
      result.privateKey must_== privateKey
    }
  }
}
