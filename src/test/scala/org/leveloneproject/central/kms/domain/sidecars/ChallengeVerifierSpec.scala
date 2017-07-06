package org.leveloneproject.central.kms.domain.sidecars

import java.util.UUID

import org.leveloneproject.central.kms.crypto.{AsymmetricVerifier, SymmetricVerifier, VerificationError, VerificationResult}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import org.mockito.Mockito._

class ChallengeVerifierSpec extends FlatSpec with Matchers with MockitoSugar {

  trait Setup {
    val asymmetricVerifier: AsymmetricVerifier = mock[AsymmetricVerifier]
    val symmetricVerifier: SymmetricVerifier = mock[SymmetricVerifier]
    val verifier = new ChallengeVerifier(asymmetricVerifier, symmetricVerifier)

    val keys: ChallengeKeys = challengeKeys()
    val answer: ChallengeAnswer = challengeAnswer()
    val challenge: String = UUID.randomUUID.toString
  }

  "verify" should "return InvalidBatchSignature error if batch signature can't be verified" in new Setup {
    when(asymmetricVerifier.verify(keys.publicKey, answer.batchSignature, challenge)).thenReturn(Left(VerificationError.InvalidSignature))

    verifier.verify(challenge, keys, answer) shouldBe Left(ChallengeError.invalidBatchSignature)
  }

  it should "return InvalidRowSignature error if row signature can't be verified" in new Setup {
    when(asymmetricVerifier.verify(keys.publicKey, answer.batchSignature, challenge)).thenReturn(Right(VerificationResult.Success))
    when(symmetricVerifier.verify(keys.symmetricKey, answer.rowSignature, challenge)).thenReturn(Left(VerificationError.InvalidSignature))
    verifier.verify(challenge, keys, answer) shouldBe Left(ChallengeError.invalidRowSignature)

  }

  it should "return successful result if both signatures verified" in new Setup {
    when(asymmetricVerifier.verify(keys.publicKey, answer.batchSignature, challenge)).thenReturn(Right(VerificationResult.Success))
    when(symmetricVerifier.verify(keys.symmetricKey, answer.rowSignature, challenge)).thenReturn(Right(VerificationResult.Success))
    verifier.verify(challenge, keys, answer) shouldBe Right(ChallengeResult.success)
  }


  private def challengeKeys() = ChallengeKeys(UUID.randomUUID.toString, UUID.randomUUID.toString)

  private def challengeAnswer() = ChallengeAnswer(UUID.randomUUID.toString, UUID.randomUUID.toString)
}
