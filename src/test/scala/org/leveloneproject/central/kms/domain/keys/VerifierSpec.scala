package org.leveloneproject.central.kms.domain.keys

import java.security.{KeyPair, PublicKey}

import net.i2p.crypto.eddsa.{EdDSAEngine, KeyPairGenerator}
import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.util.Bytes.Hex
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar

class VerifierSpec extends FlatSpec with MockitoSugar with AwaitResult {

  trait Setup {
    val keyPair: KeyPair = (new KeyPairGenerator).generateKeyPair()
    val engine = new EdDSAEngine()
    engine.initSign(keyPair.getPrivate)
    val message = "message"
    engine.update(message.getBytes())
    val signature: String = engine.sign().toHex
    val publicKey: PublicKey = keyPair.getPublic
    val verifier = new Verifier()
  }

  "verify" should "return true if signature is generated from privateKey" in new Setup {
    assert(verifier.verify(publicKey, signature, message) == Right(ValidateResponses.Success))
  }

  it should "return false if message differs from signature" in new Setup {
    assert(verifier.verify(publicKey, signature, message + " ") == Left(ValidateErrors.InvalidSignature))
  }

  it should "return false if signature differs from message" in new Setup {
    assert(verifier.verify(publicKey, signature.replace('A', 'B'), message) == Left(ValidateErrors.InvalidSignature))
  }

  it should "be able to verify many times" in new Setup {
    assert(verifier.verify(publicKey, signature, message) == Right(ValidateResponses.Success))
    assert(verifier.verify(publicKey, signature, message + " ") == Left(ValidateErrors.InvalidSignature))
    assert(verifier.verify(publicKey, signature.replace('A', 'B'), message) == Left(ValidateErrors.InvalidSignature))
    assert(verifier.verify(publicKey, signature, message) == Right(ValidateResponses.Success))
  }
}
