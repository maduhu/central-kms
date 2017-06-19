package org.leveloneproject.central.kms.domain.sidecars

import java.time.{Clock, Instant}
import java.util.UUID

import akka.actor.ActorRef
import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.keys.{CreateKeyRequest, CreateKeyResponse, KeyService}
import org.leveloneproject.central.kms.domain.Errors
import org.leveloneproject.central.kms.persistance.SidecarRepository
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class SidecarServiceSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult {

  trait Setup {
    final val sidecarRepository: SidecarRepository = mock[SidecarRepository]
    final val keyService: KeyService = mock[KeyService]
    final val sidecarList: SidecarList = mock[SidecarList]
    final val clock: Clock = mock[Clock]
    final val now: Instant = Instant.now()
    when(clock.instant()).thenReturn(now)

    final val sidecarId: UUID = UUID.randomUUID()
    final val serviceName: String = "service name"
    final val sidecarActor: ActorRef = mock[ActorRef]

    final val registerRequest = RegisterRequest(sidecarId, serviceName, sidecarActor)

    final val sidecarService = new SidecarService(sidecarRepository, keyService, clock, sidecarList)
  }

  "register" should "return error if sidecarrepo returns error" in new Setup {

    private val internalError = Errors.InternalError
    when(sidecarRepository.save(any())).thenReturn(Future.successful(Left(internalError)))
    await(sidecarService.register(registerRequest)) shouldBe Left(internalError)
  }

  it should "return error if keyService returns error" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, now)
    when(sidecarRepository.save(sidecar)).thenReturn(Future.successful(Right(sidecar)))
    when(keyService.create(CreateKeyRequest(sidecarId))).thenReturn(Future.successful(Left(Errors.InvalidRequest)))

    await(sidecarService.register(registerRequest)) shouldBe Left(Errors.InvalidRequest)
  }

  it should "return register result if sidecar and keys are saved" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, now)
    when(sidecarRepository.save(sidecar)).thenReturn(Future.successful(Right(sidecar)))
    private val keyResponse = CreateKeyResponse(UUID.randomUUID(), "public key", "private key")
    when(keyService.create(CreateKeyRequest(sidecarId))).thenReturn(Future.successful(Right(keyResponse)))

    await(sidecarService.register(registerRequest)) shouldBe Right(RegisterResponse(sidecar, keyResponse))
  }

  it should "add sidecar to sidecarList" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, now)
    when(sidecarRepository.save(sidecar)).thenReturn(Future.successful(Right(sidecar)))
    private val keyResponse = CreateKeyResponse(UUID.randomUUID(), "public key", "private key")
    when(keyService.create(CreateKeyRequest(sidecarId))).thenReturn(Future.successful(Right(keyResponse)))

    await(sidecarService.register(registerRequest))

    verify(sidecarList, times(1)).register(sidecar, sidecarActor)
  }

  "terminate" should "update terminated time in repo" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, now)
    when(sidecarRepository.terminate(sidecarId, now)).thenReturn(Future.successful(1))

    await(sidecarService.terminate(sidecar)) shouldBe Sidecar(sidecarId, serviceName, now, Some(now))
  }

  it should "remove sidecar from SidecarList" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, now)
    when(sidecarRepository.terminate(sidecarId, now)).thenReturn(Future.successful(1))

    await(sidecarService.terminate(sidecar))

    verify(sidecarList, times(1)).unregister(sidecarId)
  }
}
