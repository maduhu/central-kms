package org.leveloneproject.central.kms.sidecar

import java.time.Instant
import java.util.UUID

import akka.testkit.{TestActorRef, TestProbe}
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.batches.Batch
import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheck, HealthCheckLevel, HealthCheckStatus}
import org.leveloneproject.central.kms.domain.keys._
import org.leveloneproject.central.kms.domain.sidecars._
import org.leveloneproject.central.kms.socket.JsonResponse
import org.leveloneproject.central.kms.utils.AkkaSpec
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future
import scala.concurrent.duration._

class SidecarActorSpec extends FlatSpec with AkkaSpec with Matchers with MockitoSugar {

  trait Setup {
    val out = TestProbe()
    val sidecarActions: SidecarActions = mock[SidecarActions]
    val sidecarActor = TestActorRef(SidecarActor.props(sidecarActions))
    val defaultTimeout: FiniteDuration = 100.milliseconds

    final val serviceName = "service name"
    final val challengeString: String = UUID.randomUUID().toString
    final val sidecarId: UUID = UUID.randomUUID()
    final val commandId = "commandId"
    final val publicKey = "some public key"
    final val privateKey = "some private key"
    final val symmetricKey = "symmetric key"

    def setupRegistration(): Sidecar = {
      val keyResponse = CreateKeyResponse(sidecarId, publicKey, privateKey, symmetricKey)
      val sidecar = Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString)
      when(sidecarActions.registerSidecar(RegisterParameters(sidecarId, serviceName))).thenReturn(Future.successful(Right(RegisterResponse(sidecar, keyResponse))))
      sidecar
    }

    def connectSidecar(): Unit = {
      sidecarActor ! Connected(out.ref)
      out.expectNoMsg(defaultTimeout)
    }

    def registerSidecar(): Unit = {
      sidecarActor ! Register(commandId, RegisterParameters(sidecarId, serviceName))
    }

    def connectAndRegisterSidecar(): Sidecar = {
      connectSidecar()
      registerSidecar()
      expectRegistrationResponse()
    }

    def acceptChallenge(sidecar: Sidecar): Unit = {

      when(sidecarActions.challenge(any(), any(), any())).thenReturn(Future.successful(Right(SidecarAndActor(sidecar, sidecarActor))))
      val commandId = UUID.randomUUID().toString
      sidecarActor ! Challenge(commandId, ChallengeAnswer("", ""))

      out.expectMsg(Responses.challengeAccepted(commandId, ChallengeResult.success))
    }

    def expectRegistrationResponse(): Sidecar = {
      out.expectMsg(Responses.sidecarRegistered(commandId, RegisteredResult(sidecarId, privateKey, symmetricKey, challengeString)))
      Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString)
    }
  }

  "initial" should "not respond to register command when not connected" in new Setup {
    sidecarActor ! Register(commandId, RegisterParameters(UUID.randomUUID(), ""))

    out.expectNoMsg(defaultTimeout)
  }

  it should "accept Connected message" in new Setup {
    connectSidecar()
  }

  "when connected" should "stop self on Disconnect command" in new Setup {
    connectSidecar()

    sidecarActor ! Disconnect

    sidecarActor.underlying.isTerminated shouldBe true
  }

  it should "send responses to out" in new Setup {
    connectSidecar()

    private val response = Responses.commandError(commandId, KmsError.parseError)
    sidecarActor ! response

    out.expectMsg(response)
  }

  it should "send error to out when registration fails" in new Setup {
    val error = KmsError(500, "some message")
    when(sidecarActions.registerSidecar(RegisterParameters(sidecarId, serviceName))).thenReturn(Future.successful(Left(error)))
    connectSidecar()
    registerSidecar()
    out.expectMsg(Responses.commandError(commandId, error))
  }

  it should "send registered to out when registering" in new Setup {
    setupRegistration()
    connectAndRegisterSidecar()
  }

  it should "send method not allowed to out when registering twice" in new Setup {
    setupRegistration()

    connectAndRegisterSidecar()
    registerSidecar()
    out.expectMsg(JsonResponse("2.0", None, Some(KmsError.methodNotAllowed("register")), commandId))
  }

  it should "send duplicate sidecar registered error to out" in new Setup {
    private val exists = KmsError.sidecarExistsError(sidecarId)
    when(sidecarActions.registerSidecar(any())).thenReturn(Future.successful(Left(exists)))
    connectSidecar()
    registerSidecar()
    out.expectMsg(Responses.commandError(commandId, exists))
  }

  "when challenged" should "not respond to batch commands" in new Setup {
    setupRegistration()
    connectAndRegisterSidecar()

    private val command = SaveBatch(commandId, SaveBatchParameters(UUID.randomUUID(), "signature"))
    sidecarActor ! command

    out.expectMsg(Responses.methodNotAllowed(command))
  }

  it should "send responses to out" in new Setup {
    connectSidecar()

    private val response = Responses.commandError(commandId, KmsError.parseError)
    sidecarActor ! response

    out.expectMsg(response)
  }

  it should "send challenge response on registration request" in new Setup {
    setupRegistration()
    connectSidecar()
    registerSidecar()

    out.expectMsg(Responses.sidecarRegistered(commandId, RegisteredResult(sidecarId, privateKey, symmetricKey, challengeString)))
  }

  it should "send error to socket if challenge fails" in new Setup {
    setupRegistration()
    connectAndRegisterSidecar()

    private val challengeError = ChallengeError.invalidRowSignature
    when(sidecarActions.challenge(any(), any(), any())).thenReturn(Future.successful(Left(challengeError)))

    sidecarActor ! Challenge(commandId, ChallengeAnswer("", ""))
    out.expectMsg(Responses.commandError(commandId, challengeError))
  }

  it should "respond ok to challenge command" in new Setup {
    setupRegistration()
    private val sidecar = connectAndRegisterSidecar()

    when(sidecarActions.challenge(any(), any(), any())).thenReturn(Future.successful(Right(SidecarAndActor(sidecar, sidecarActor))))

    sidecarActor ! Challenge(commandId, ChallengeAnswer("batchSignature", "rowSignature"))

    out.expectMsg(Responses.challengeAccepted(commandId, ChallengeResult.success))
  }

  "when registered" should "save batch when registered and send batch to out" in new Setup {
    private val sidecar = setupRegistration()
    connectAndRegisterSidecar()
    acceptChallenge(sidecar)

    private val batchId = UUID.randomUUID()
    private val signature = "signature"

    private val batch = Batch(batchId, sidecarId, signature, Instant.now())
    private val parameters = SaveBatchParameters(batchId, signature)
    when(sidecarActions.createBatch(sidecar, parameters))
      .thenReturn(Future.successful(Right(batch)))

    sidecarActor ! SaveBatch(commandId, parameters)
    out.expectMsg(Responses.batchCreated(commandId, batch))
  }

  it should "send health check request to web socket" in new Setup {
    private val sidecar = setupRegistration()
    connectAndRegisterSidecar()
    acceptChallenge(sidecar)

    private val healthCheckId = UUID.randomUUID()
    private val healthCheckLevel = HealthCheckLevel.Ping
    private val healthCheck = HealthCheck(healthCheckId, sidecarId, healthCheckLevel, Instant.now(), HealthCheckStatus.Pending)

    sidecarActor ! healthCheck

    out.expectMsg(Responses.healthCheckRequest(healthCheck))
  }

  it should "terminate sidecar and stop self when disconnected" in new Setup {
    private val sidecar = setupRegistration()
    connectAndRegisterSidecar()
    acceptChallenge(sidecar)

    when(sidecarActions.terminateSidecar(sidecar)).thenReturn(Future.successful(Right(sidecar)))

    sidecarActor ! Disconnect
    sidecarActor.underlying.isTerminated shouldBe true
  }
}

case class SomeCommand(id: String) extends Command("some name")
