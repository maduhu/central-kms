package org.leveloneproject.central.kms.crypto

import org.leveloneproject.central.kms.util.Bytes._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Try}

class TweetNaClKeys extends AsymmetricKeyGenerator with AsymmetricVerifier {
  def generate(): Future[PublicPrivateKeyPair] = Future {
    val publicKey = Array.ofDim[Byte](TweetNaCl.SIGN_PUBLIC_KEY_BYTES)
    val privateKey = Array.ofDim[Byte](TweetNaCl.SIGN_SECRET_KEY_BYTES)

    TweetNaCl.crypto_sign_keypair(publicKey, privateKey, false)
    PublicPrivateKeyPair(publicKey.toHex, privateKey.toHex)
  }

  def verify(publicKey: Array[Byte], signature: Array[Byte], message: Array[Byte]): Either[VerificationError, VerificationResult] = {
    Try(TweetNaCl.crypto_sign_open(signature, publicKey)) match {
      case Success(unsigned) if unsigned.deep == message.deep ⇒ Right(VerificationResult.Success)
      case _ ⇒ Left(VerificationError.InvalidSignature)
    }
  }

  def verify(publicKey: String, signature: String, message: String): Either[VerificationError, VerificationResult] = verify(publicKey.fromHex, signature.fromHex, message.fromUtf8)
}
