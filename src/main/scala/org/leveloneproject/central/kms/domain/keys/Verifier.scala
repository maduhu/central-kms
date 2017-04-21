package org.leveloneproject.central.kms.domain.keys

import java.security.PublicKey

import net.i2p.crypto.eddsa.EdDSAEngine
import org.leveloneproject.central.kms.domain.keys.KeyDomain._
import org.leveloneproject.central.kms.util.Bytes

class Verifier() {
  private val signer = new EdDSAEngine()

  def verify(publicKey: PublicKey, signature: String, message: String): Either[ValidateError, ValidateResponse] = {
    signer.initVerify(publicKey)
    signer.update(message.getBytes)
    if (signer.verify(Bytes.fromHex(signature))) {
      Right(ValidateResponses.Success)
    } else {
      Left(ValidateErrors.InvalidSignature)
    }
  }
}
