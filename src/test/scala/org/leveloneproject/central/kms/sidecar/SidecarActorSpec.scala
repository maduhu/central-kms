package org.leveloneproject.central.kms.sidecar

import java.time.Instant
import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.TestProbe
import org.leveloneproject.central.kms.domain.batches._
import org.leveloneproject.central.kms.domain.keys.KeyDomain._
import org.leveloneproject.central.kms.domain.keys._
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.sidecar.batch._
import org.leveloneproject.central.kms.sidecar.registration._
import org.leveloneproject.central.kms.utils.AkkaSpec
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future
import scala.concurrent.duration._

class SidecarActorSpec extends FlatSpec with AkkaSpec with MockitoSugar {

  trait Setup {
    val out = TestProbe()
    val outRef: ActorRef = out.ref
    val keyService: KeyService = mock[KeyService]
    val batchService: BatchService = mock[BatchService]
    val sidecarActor: ActorRef = system.actorOf(SidecarActor.props(keyService, batchService))
    val defaultTimeout: FiniteDuration = 100.milliseconds

    final val serviceName = "service name"
    final val sidecarId: UUID = UUID.randomUUID()
    final val commandId = "commandId"
    final val privateKey = "some private key"

    def setupRegistration(): OngoingStubbing[Future[Either[Error, KeyResponse]]] = {
      when(keyService.create(KeyRequest(sidecarId, serviceName))).thenReturn(Future.successful(Right(KeyResponse(sidecarId, serviceName, privateKey))))
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

  it should "not respond to register command when not connected" in new Setup {
    sidecarActor ! RegisterCommand(commandId, RegisterParameters(UUID.randomUUID(), ""))

    out.expectNoMsg(defaultTimeout)
  }

  it should "send error to out when key service throws error" in new Setup {
    val error = Error(500, "some message")
    when(keyService.create(KeyRequest(sidecarId, serviceName))).thenReturn(Future.successful(Left(error)))

    connectAndRegisterSidecar()

    out.expectMsg(ErrorWithCommandId(error, commandId))
  }

  it should "send registered to out when registering" in new Setup {
    setupRegistration()
    connectAndRegisterSidecar()
    out.expectMsg(Registered(commandId, RegisteredResult(sidecarId, privateKey, "")))
  }

  it should "send method not allowed to out when registering twice" in new Setup {
    setupRegistration()

    connectAndRegisterSidecar()
    out.expectMsg(Registered(commandId, RegisteredResult(sidecarId, privateKey, "")))
    registerSidecar()
    out.expectMsg(ErrorWithCommandId(Error(100, "'register' method not allowed in current state"), commandId))
  }

  it should "send duplicate sidecar registered error to out" in new Setup {
    private val exists = Errors.SidecarExistsError(sidecarId)
    when(keyService.create(any())).thenReturn(Future.successful(Left(exists)))
    connectAndRegisterSidecar()
    out.expectMsg(ErrorWithCommandId(exists, commandId))
  }

  it should "save batch when registered and send batch to out" in new Setup {
    setupRegistration()
    connectAndRegisterSidecar()
    out.expectMsg(Registered(commandId, RegisteredResult(sidecarId, privateKey, "")))

    private val batchId = UUID.randomUUID()
    private val signature = "signature"

    when(batchService.create(CreateRequest(sidecarId,batchId, signature)))
      .thenReturn(Future.successful(Right(Batch(batchId, sidecarId, signature, Instant.now()))))

    sidecarActor ! BatchCommand(commandId, BatchParameters(batchId, signature))
    out.expectMsg(BatchCreated(commandId, BatchCreatedResult(batchId)))
  }
}
