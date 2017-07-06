package org.leveloneproject.central.kms.crypto

case class VerificationResult(status: String)

object VerificationResult {
  final val Success = VerificationResult("OK")
}
