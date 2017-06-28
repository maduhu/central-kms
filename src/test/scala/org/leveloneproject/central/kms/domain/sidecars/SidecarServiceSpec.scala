package org.leveloneproject.central.kms.domain.sidecars

import java.time.{Clock, Instant}
import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.TestProbe
import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.keys.{CreateKeyRequest, CreateKeyResponse, KeyService}
import org.leveloneproject.central.kms.persistance.{SidecarLogsRepository, SidecarRepository}
import org.leveloneproject.central.kms.util.{ChallengeGenerator, IdGenerator}
import org.leveloneproject.central.kms.utils.AkkaSpec
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class SidecarServiceSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult with AkkaSpec {

  trait Setup {
    final val sidecarRepository: SidecarRepository = mock[SidecarRepository]
    final val sidecarLogsRepository: SidecarLogsRepository = mock[SidecarLogsRepository]
    final val keyService: KeyService = mock[KeyService]
    final val sidecarList: SidecarList = mock[SidecarList]
    final val clock: Clock = mock[Clock]
    final val now: Instant = Instant.now()
    when(clock.instant()).thenReturn(now)

    final val sidecarId: UUID = UUID.randomUUID()
    final val logId: UUID = UUID.randomUUID()
    final val serviceName: String = "service name"
    final val sidecarActor: ActorRef = TestProbe().ref

    final val registerRequest = RegisterRequest(sidecarId, serviceName)

    final val challengeString: String = UUID.randomUUID().toString
    final val sidecarService = new SidecarService(sidecarRepository, keyService, clock, sidecarList, sidecarLogsRepository) with ChallengeGenerator with IdGenerator {
      override def newChallenge(): String = challengeString

      override def newId(): UUID = logId
    }
  }

  "register" should "return error if SideCar Repository returns error" in new Setup {

    private val internalError = KmsError.internalError
    when(sidecarRepository.insert(any())).thenReturn(Future.successful(Left(internalError)))
    await(sidecarService.register(registerRequest)) shouldBe Left(internalError)
  }

  it should "return error if keyService returns error" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString)
    when(sidecarRepository.insert(sidecar)).thenReturn(Future.successful(Right(sidecar)))
    when(keyService.create(CreateKeyRequest(sidecarId))).thenReturn(Future.successful(Left(KmsError.invalidRequest)))

    await(sidecarService.register(registerRequest)) shouldBe Left(KmsError.invalidRequest)
  }

  it should "return register result if sidecar and keys are saved" in new Setup {
    private val initialized = SidecarStatus.Challenged
    private val sidecar = Sidecar(sidecarId, serviceName, initialized, challengeString)
    when(sidecarRepository.insert(sidecar)).thenReturn(Future.successful(Right(sidecar)))
    private val keyResponse = CreateKeyResponse(UUID.randomUUID(), "public key", "private key", "symmetric key")
    when(keyService.create(CreateKeyRequest(sidecarId))).thenReturn(Future.successful(Right(keyResponse)))
    when(sidecarLogsRepository.save(any())(any())).thenReturn(Future.successful(Right(SidecarLog(logId, sidecarId, now, initialized))))

    await(sidecarService.register(registerRequest)) shouldBe Right(RegisterResponse(sidecar, keyResponse))
  }

  it should "add initialized to logs" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString)
    private val log = SidecarLog(logId, sidecarId, now, SidecarStatus.Challenged, None)
    private val keyResponse = CreateKeyResponse(UUID.randomUUID(), "public key", "private key", "symmetric key")

    when(sidecarRepository.insert(sidecar)).thenReturn(Future.successful(Right(sidecar)))
    when(sidecarLogsRepository.save(log)).thenReturn(Future.successful(Right(log)))
    when(keyService.create(CreateKeyRequest(sidecarId))).thenReturn(Future.successful(Right(keyResponse)))

    await(sidecarService.register(registerRequest))

    verify(sidecarLogsRepository, times(1)).save(log)
  }

  "challengeAccepted" should "add sidecar to sidecarList" in new Setup {
    when(sidecarRepository.updateStatus(sidecarId, SidecarStatus.Registered)).thenReturn(Future.successful(Right(1)))
    when(sidecarLogsRepository.save(any())(any())).thenReturn(Future.successful(Right(SidecarLog(logId, sidecarId, now, SidecarStatus.Registered))))
    private val sidecar = Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString)

    private val sidecarWithActor = SidecarAndActor(sidecar, sidecarActor)

    await(sidecarService.challengeAccepted(sidecarWithActor))

    verify(sidecarList, times(1)).register(SidecarAndActor(sidecar.copy(status = SidecarStatus.Registered),sidecarActor))
  }

  "terminate" should "insert terminated log in repo" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString)

    private val log = SidecarLog(logId, sidecarId, now, SidecarStatus.Terminated, None)
    when(sidecarLogsRepository.save(log)).thenReturn(Future.successful(Right(log)))
    when(sidecarRepository.updateStatus(sidecarId, SidecarStatus.Terminated)).thenReturn(Future.successful(Right(1)))

    await(sidecarService.terminate(sidecar)) shouldBe Right(Sidecar(sidecarId, serviceName, SidecarStatus.Terminated, challengeString))

    verify(sidecarLogsRepository, times(1)).save(log)
    verify(sidecarRepository, times(1)).updateStatus(sidecarId, SidecarStatus.Terminated)
  }

  it should "remove sidecar from SidecarList" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString)
    when(sidecarLogsRepository.save(any())(any())).thenReturn(Future.successful(Right(SidecarLog(logId, sidecarId, now, SidecarStatus.Terminated))))
    when(sidecarRepository.updateStatus(sidecarId, SidecarStatus.Terminated)).thenReturn(Future.successful(Right(1)))

    await(sidecarService.terminate(sidecar))

    verify(sidecarList, times(1)).unregister(sidecarId)
  }
}
