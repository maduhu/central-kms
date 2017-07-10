package org.leveloneproject.central.kms.sidecar

import java.util.UUID

import akka.testkit.TestProbe
import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.batches.BatchCreatorImpl
import org.leveloneproject.central.kms.domain.healthchecks.HealthCheckService
import org.leveloneproject.central.kms.domain.inquiries.InquiryResponseVerifier
import org.leveloneproject.central.kms.domain.sidecars._
import org.leveloneproject.central.kms.utils.AkkaSpec
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class SidecarActionsSpec extends FlatSpec with Matchers with MockitoSugar with AkkaSpec with AwaitResult {

  trait Setup {
    val challengeVerifier: ChallengeVerifier = mock[ChallengeVerifier]
    val sidecarService: SidecarService = mock[SidecarService]
    val inquiryResponseVerifier: InquiryResponseVerifier = mock[InquiryResponseVerifier]
    val actions = new SidecarActions(mock[BatchCreatorImpl], sidecarService, mock[HealthCheckService], challengeVerifier, inquiryResponseVerifier)
    val challenge: String = randomString
    val keys = ChallengeKeys(randomString, randomString)
    val answer = ChallengeAnswer(randomString, randomString)
    val sidecarAndActor = SidecarAndActor(sidecar(challenge), TestProbe().ref)
  }

  "challenge" should "accept challenge if verification passes" in new Setup {
    when(challengeVerifier.verify(challenge, keys, answer)).thenReturn(Right(ChallengeResult.success))
    when(sidecarService.challengeAccepted(sidecarAndActor)).thenReturn(Future(Right(sidecarAndActor)))
    private val result = await(actions.challenge(sidecarAndActor, keys, answer))
    result shouldBe Right(sidecarAndActor)
    verify(sidecarService, times(1)).challengeAccepted(sidecarAndActor)
    verify(challengeVerifier, times(1)).verify(challenge, keys, answer)
  }

  it should "return error if verification fails" in new Setup {
    private val invalidRowSignature = ChallengeError.invalidRowSignature
    when(challengeVerifier.verify(challenge, keys, answer)).thenReturn(Left(invalidRowSignature))
    when(sidecarService.suspend(sidecarAndActor.sidecar, invalidRowSignature.message)).thenReturn(Future(Right(sidecarAndActor.sidecar)))

    await(actions.challenge(sidecarAndActor, keys, answer)) shouldBe Left(invalidRowSignature)

    verify(sidecarService, times(0)).challengeAccepted(sidecarAndActor)
    verify(sidecarService, times(1)).suspend(sidecarAndActor, invalidRowSignature.message)
    verify(challengeVerifier, times(1)).verify(challenge, keys, answer)
  }

  private def sidecar(challenge: String) = {
    Sidecar(UUID.randomUUID, randomString, SidecarStatus.Challenged, challenge)
  }

  def randomString: String = UUID.randomUUID.toString
}
