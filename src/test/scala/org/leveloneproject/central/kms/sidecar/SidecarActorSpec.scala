package org.leveloneproject.central.kms.sidecar

import java.time.Instant
import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.{TestActorRef, TestProbe}
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.domain.batches._
import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheck, HealthCheckLevel, HealthCheckStatus}
import org.leveloneproject.central.kms.domain.keys._
import org.leveloneproject.central.kms.domain.sidecars.{RegisterResponse, Sidecar}
import org.leveloneproject.central.kms.sidecar.batch._
import org.leveloneproject.central.kms.sidecar.healthcheck.HealthCheckRequest
import org.leveloneproject.central.kms.sidecar.registration._
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
    val outRef: ActorRef = out.ref
    val sidecarSupport: SidecarSupport = mock[SidecarSupport]
    val sidecarActor = TestActorRef(SidecarActor.props(sidecarSupport))
    val defaultTimeout: FiniteDuration = 100.milliseconds

    final val serviceName = "service name"
    final val sidecarId: UUID = UUID.randomUUID()
    final val commandId = "commandId"
    final val publicKey = "some public key"
    final val privateKey = "some private key"
    final val symmetricKey = "symmetric key";

    def setupRegistration(): Sidecar = {
      val keyResponse = CreateKeyResponse(sidecarId, publicKey, privateKey, symmetricKey)
      val sidecar = Sidecar(sidecarId, serviceName, Instant.now())
      when(sidecarSupport.registerSidecar(RegisterParameters(sidecarId, serviceName), sidecarActor)).thenReturn(Future.successful(Right(RegisterResponse(sidecar, keyResponse))))
      sidecar
    }

    def connectSidecar(): Unit = {
      sidecarActor ! SidecarActor.Connected(outRef)
      out.expectNoMsg(defaultTimeout)
    }

    def registerSidecar(): Unit = {
      sidecarActor ! RegisterCommand(commandId, RegisterParameters(sidecarId, serviceName))
    }

    def connectAndRegisterSidecar(): Unit = {
      connectSidecar()
      registerSidecar()
    }
  }

  "sidecar actor" should "accept Sidecar.Connect message" in new Setup {
    connectSidecar()
  }

  it should "stop self on Disconnect command" in new Setup {
    connectSidecar()

    sidecarActor ! SidecarActor.Disconnect

    sidecarActor.underlying.isTerminated shouldBe true
  }

  it should "not respond to register command when not connected" in new Setup {
    sidecarActor ! RegisterCommand(commandId, RegisterParameters(UUID.randomUUID(), ""))

    out.expectNoMsg(defaultTimeout)
  }

  it should "send error to out when sidecar service throws error" in new Setup {
    val error = Error(500, "some message")
    when(sidecarSupport.registerSidecar(RegisterParameters(sidecarId, serviceName), sidecarActor)).thenReturn(Future.successful(Left(error)))
    connectAndRegisterSidecar()

    out.expectMsg(ErrorWithCommandId(error, commandId))
  }

  it should "send registered to out when registering" in new Setup {
    setupRegistration()
    connectAndRegisterSidecar()
    out.expectMsg(Registered(commandId, RegisteredResult(sidecarId, privateKey, symmetricKey)))
  }

  it should "send method not allowed to out when registering twice" in new Setup {
    setupRegistration()

    connectAndRegisterSidecar()
    out.expectMsg(Registered(commandId, RegisteredResult(sidecarId, privateKey, symmetricKey)))
    registerSidecar()
    out.expectMsg(ErrorWithCommandId(Error(100, "'register' method not allowed in current state"), commandId))
  }

  it should "send duplicate sidecar registered error to out" in new Setup {
    private val exists = Errors.SidecarExistsError(sidecarId)
    when(sidecarSupport.registerSidecar(any(), any())).thenReturn(Future.successful(Left(exists)))
    connectAndRegisterSidecar()
    out.expectMsg(ErrorWithCommandId(exists, commandId))
  }

  it should "save batch when registered and send batch to out" in new Setup {
    setupRegistration()
    connectAndRegisterSidecar()
    out.expectMsg(Registered(commandId, RegisteredResult(sidecarId, privateKey, symmetricKey)))

    private val batchId = UUID.randomUUID()
    private val signature = "signature"

    when(sidecarSupport.createBatch(sidecarId, BatchParameters(batchId, signature)))
      .thenReturn(Future.successful(Right(Batch(batchId, sidecarId, signature, Instant.now()))))

    sidecarActor ! BatchCommand(commandId, BatchParameters(batchId, signature))
    out.expectMsg(BatchCreated(commandId, BatchCreatedResult(batchId)))
  }

  it should "send healthcheck request to websocket" in new Setup {
    setupRegistration()
    connectAndRegisterSidecar()
    out.expectMsg(Registered(commandId, RegisteredResult(sidecarId, privateKey, symmetricKey)))

    private val healthCheckId = UUID.randomUUID()
    private val healthCheckLevel = HealthCheckLevel.Ping
    private val healthCheck = HealthCheck(healthCheckId, sidecarId, healthCheckLevel, Instant.now(), HealthCheckStatus.Pending, None, None)

    sidecarActor ! healthCheck

    out.expectMsg(HealthCheckRequest(healthCheckId, healthCheckLevel))
  }

  it should "terminate sidecar and stop self when disconnected" in new Setup {
    private val sidecar = setupRegistration()
    connectAndRegisterSidecar()
    out.expectMsg(Registered(commandId, RegisteredResult(sidecarId, privateKey, symmetricKey)))

    when(sidecarSupport.terminateSidecar(sidecar)).thenReturn(Future.successful(sidecar))

    sidecarActor ! SidecarActor.Disconnect
    sidecarActor.underlying.isTerminated shouldBe true
  }
}
