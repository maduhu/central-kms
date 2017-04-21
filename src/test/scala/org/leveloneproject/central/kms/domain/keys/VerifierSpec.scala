package org.leveloneproject.central.kms.domain.keys

import java.security.{KeyPair, PublicKey}

import net.i2p.crypto.eddsa.{EdDSAEngine, KeyPairGenerator}
import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.keys.KeyDomain._
import org.leveloneproject.central.kms.util.Bytes
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class VerifierSpec extends Specification with AwaitResult {
  trait Setup extends Scope {
    val keyPair: KeyPair = (new KeyPairGenerator).generateKeyPair()
    val engine = new EdDSAEngine()
    engine.initSign(keyPair.getPrivate)
    val message = "message"
    engine.update(message.getBytes())
    val signature: String = Bytes.toHex(engine.sign())
    val publicKey: PublicKey = keyPair.getPublic
    val verifier = new Verifier()
  }

  "verify" should {
    "return true if signature is generated from privateKey" in new Setup {
      verifier.verify(publicKey, signature, message) must beRight(ValidateResponses.Success)
    }

    "return false if message differs from signature" in new Setup {
      verifier.verify(publicKey, signature, message + " ") must beLeft(ValidateErrors.InvalidSignature)
    }

    "return false if signature differs from message" in new Setup {
      verifier.verify(publicKey, signature.replace('A', 'B'), message) must beLeft(ValidateErrors.InvalidSignature)
    }

    "be able to verify many times" in new Setup {
      verifier.verify(publicKey, signature, message) must beRight(ValidateResponses.Success)
      verifier.verify(publicKey, signature, message + " ") must beLeft(ValidateErrors.InvalidSignature)
      verifier.verify(publicKey, signature.replace('A', 'B'), message) must beLeft(ValidateErrors.InvalidSignature)
      verifier.verify(publicKey, signature, message) must beRight(ValidateResponses.Success)
    }
  }
}
