package org.leveloneproject.central.kms.domain.keys

import java.security.{KeyPair, KeyPairGeneratorSpi, PrivateKey, PublicKey}
import java.util.UUID

import net.i2p.crypto.eddsa.KeyPairGenerator
import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.util.Bytes
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class KeyGeneratorSpec extends Specification with Mockito with AwaitResult {
  private val privateKeyString = "00112233445566778899"
  private val publicKeyString = "AABBCCDDEEFF"

  trait Setup extends Scope {
    val keyPairGenerator: KeyPairGeneratorSpi = mock[KeyPairGeneratorSpi]
    val keyGenerator = new KeyGenerator(keyPairGenerator)
  }

  "KeyGenerator" should {
    "generate pair" in new Setup {
      private val privateKey = mock[PrivateKey]
      privateKey.getEncoded returns Bytes.fromHex(privateKeyString)
      private val publicKey = mock[PublicKey]
      publicKey.getEncoded returns Bytes.fromHex(publicKeyString)
      val keyPair = new KeyPair(publicKey, privateKey)
      keyPairGenerator.generateKeyPair returns keyPair

      private val result = await(keyGenerator.generate())
      result.id must_== UUID.fromString(result.id.toString)
      result.privateKey must_== privateKeyString
      result.publicKey must_== publicKeyString
    }

    "generate different keys on successive calls" in {
      val keyGen = new KeyGenerator(new KeyPairGenerator)

      val result1 = await(keyGen.generate())
      val result2 = await(keyGen.generate())
      result1.privateKey must_!= result2.privateKey
      result1.publicKey must_!= result2.publicKey
    }
  }
}
