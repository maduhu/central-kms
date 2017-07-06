package org.leveloneproject.central.kms.domain.sidecars

import com.google.inject.Inject
import org.leveloneproject.central.kms.crypto.{AsymmetricVerifier, SymmetricVerifier, VerificationResult}
import org.leveloneproject.central.kms.domain.KmsError

class ChallengeVerifier @Inject()(asymmetricVerifier: AsymmetricVerifier, symmetricVerifier: SymmetricVerifier) {
  def verify(challenge: String, keys: ChallengeKeys, answer: ChallengeAnswer): Either[KmsError, ChallengeResult] = {
    for {
      _ ← verifyBatchSignature(challenge, keys, answer)
      _ ← verifyRowSignature(challenge, keys, answer)
    } yield ChallengeResult.success
  }

  private def verifyBatchSignature(challenge: String, variables: ChallengeKeys, answer: ChallengeAnswer): Either[KmsError, VerificationResult] = {
    asymmetricVerifier.verify(variables.publicKey, answer.batchSignature, challenge)
      .fold(_ ⇒ Left(ChallengeError.invalidBatchSignature), r ⇒ Right(r))
  }

  private def verifyRowSignature(challenge: String, variables: ChallengeKeys, answer: ChallengeAnswer): Either[KmsError, VerificationResult] = {
    symmetricVerifier.verify(variables.symmetricKey, answer.rowSignature, challenge)
      .fold(_ ⇒ Left(ChallengeError.invalidRowSignature), result ⇒ Right(result))
  }
}

case class ChallengeResult(status: String)

object ChallengeResult {
  val success = ChallengeResult("OK")
}

case class ChallengeKeys(publicKey: String, symmetricKey: String)

case class ChallengeAnswer(batchSignature: String, rowSignature: String)

object ChallengeError {
  val invalidRowSignature = KmsError(1001, "Invalid row signature")

  val invalidBatchSignature = KmsError(1000, "Invalid batch signature")
}

