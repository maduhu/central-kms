package org.leveloneproject.central.kms.crypto

import javax.crypto.spec.SecretKeySpec
import javax.crypto.{KeyGenerator, Mac}

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.leveloneproject.central.kms.util.Bytes._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CmacKeys extends SymmetricKeyGenerator with SymmetricVerifier {

  private val keySize = 256
  private val keyAlgorithm = "AES"
  private val hashAlgorithm = "AESCMAC"

  override def generate(): Future[String] = Future {
    val generator = KeyGenerator.getInstance(keyAlgorithm)
    generator.init(keySize)
    generator.generateKey().getEncoded.toHex
  }

  def verify(key: String, signature: String, message: String): Either[VerificationError, VerificationResult] = {
    for {
      mac ← getMac(key)
      hash ← hash(message, mac)
      result ← compare(hash, signature)
    } yield result
  }

  private def compare(hash: Array[Byte], signature: String): Either[VerificationError, VerificationResult] = {
    if (hash.deep == signature.fromHex.deep) {
      Right(VerificationResult.Success)
    } else {
      Left(VerificationError.InvalidSignature)
    }
  }

  private def hash(message: String, mac: Mac): Either[VerificationError, Array[Byte]] = {
    try {
      val messageBytes = message.getBytes
      mac.update(messageBytes, 0, messageBytes.length)
      val out = Array.ofDim[Byte](mac.getMacLength)
      mac.doFinal(out, 0)
      Right(out)
    } catch {
      case _: Throwable ⇒ Left(VerificationError.InvalidSignature)
    }
  }

  private def getMac(key: String): Either[VerificationError, Mac] = {
    try {
      val mac = Mac.getInstance(hashAlgorithm, BouncyCastleProvider.PROVIDER_NAME)
      val keySpec = new SecretKeySpec(key.fromHex, keyAlgorithm)
      mac.init(keySpec)
      Right(mac)
    } catch {
      case _: Throwable ⇒ Left(VerificationError.InvalidKey)
    }
  }
}
