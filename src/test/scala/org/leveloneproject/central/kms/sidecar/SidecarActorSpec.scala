package org.leveloneproject.central.kms.sidecar

import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.TestProbe
import org.leveloneproject.central.kms.domain.keys.KeyDomain.{KeyRequest, KeyResponse}
import org.leveloneproject.central.kms.domain.keys.{CreateError, KeyService}
import org.leveloneproject.central.kms.domain.{Error, ErrorWithCommandId}
import org.leveloneproject.central.kms.socket.RpcErrors
import org.leveloneproject.central.kms.utils.AkkaSpec
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future
import scala.concurrent.duration._

class SidecarActorSpec extends FlatSpec with AkkaSpec with MockitoSugar {

  trait Setup {
    val out = TestProbe()
    val outRef: ActorRef = out.ref
    val keyService: KeyService = mock[KeyService]
    val sidecarActor: ActorRef = system.actorOf(SidecarActor.props(keyService))
    val defaultTimeout: FiniteDuration = 500.milliseconds

    final val serviceName = "service name"
    final val sidecarId: UUID = UUID.randomUUID()
    final val commandId = "commandId"
  }

  "sidecar actor" should "accept Sidecar.Connect message" in new Setup {
    sidecarActor ! SidecarActor.Connected(outRef)
    out.expectNoMsg(500.milliseconds)
  }

  it should "not respond to register command when not connected" in new Setup {
    sidecarActor ! RegisterCommand(commandId, RegisterParameters(UUID.randomUUID(), ""))

    out.expectNoMsg(defaultTimeout)
  }

  it should "send error to out when key service throws error" in new Setup {
    val exception = new Exception()
    when(keyService.create(KeyRequest(sidecarId, serviceName))).thenReturn(Future.failed(exception))

    sidecarActor ! SidecarActor.Connected(outRef)

    sidecarActor ! RegisterCommand(commandId, RegisterParameters(sidecarId, serviceName))
    out.expectMsg(defaultTimeout, RpcErrors.InternalError)
  }

  it should "send registered to out when registering" in new Setup {
    sidecarActor ! SidecarActor.Connected(outRef)

    out.expectNoMsg(500.milliseconds)

    private val privateKey = "some private key"
    when(keyService.create(KeyRequest(sidecarId, serviceName))).thenReturn(Future.successful(Right(KeyResponse(sidecarId, serviceName, privateKey))))

    sidecarActor ! RegisterCommand(commandId, RegisterParameters(sidecarId, serviceName))
    out.expectMsg(Registered(commandId, RegisteredResult(sidecarId, privateKey, "")))
  }

  it should "send method not allowed to out when registering twice" in new Setup {
    sidecarActor ! SidecarActor.Connected(outRef)

    out.expectNoMsg(500.milliseconds)

    private val privateKey = "some private key"
    when(keyService.create(KeyRequest(sidecarId, serviceName))).thenReturn(Future.successful(Right(KeyResponse(sidecarId, serviceName, privateKey))))

    sidecarActor ! RegisterCommand(commandId, RegisterParameters(sidecarId, serviceName))
    out.expectMsg(Registered(commandId, RegisteredResult(sidecarId, privateKey, "")))
    sidecarActor ! RegisterCommand(commandId, RegisterParameters(sidecarId, serviceName))
    out.expectMsg(defaultTimeout, ErrorWithCommandId(Error(100, "'register' method not allowed in current state"), commandId))
  }

  it should "send duplicate sidecar registered error to out" in new Setup {

    private val exists = CreateError.KeyExists(sidecarId)
    when(keyService.create(any())).thenReturn(Future.successful(Left(exists)))
    sidecarActor ! SidecarActor.Connected(outRef)

    sidecarActor ! RegisterCommand(commandId, RegisterParameters(sidecarId, serviceName))

    out.expectMsg(SidecarErrors.DuplicateSidecarRegistered(commandId, exists.message))
  }
}
