package org.leveloneproject.central.kms.crypto

case class VerificationError(error: String)

object VerificationError {
  final val KeyNotFound = VerificationError("Key not found")
  final val InvalidSignature = VerificationError("Invalid signature")
  final val InvalidKey = VerificationError("Key is not in valid format")
}