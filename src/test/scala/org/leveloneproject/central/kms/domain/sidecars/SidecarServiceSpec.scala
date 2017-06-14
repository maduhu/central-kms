package org.leveloneproject.central.kms.domain.sidecars

import java.time.{Clock, Instant}
import java.util.UUID

import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.keys.{CreateKeyRequest, CreateKeyResponse, KeyService}
import org.leveloneproject.central.kms.domain.{Errors, Sidecar}
import org.leveloneproject.central.kms.persistance.SidecarRepository
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class SidecarServiceSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult {

  trait Setup {
    val sidecarRepository: SidecarRepository = mock[SidecarRepository]
    val keyService: KeyService = mock[KeyService]
    val clock: Clock = mock[Clock]

    val now: Instant = Instant.now()
    val sidecarId: UUID = UUID.randomUUID()
    val serviceName: String = "service name"

    when(clock.instant()).thenReturn(now)
    val sidecarService = new SidecarService(sidecarRepository, keyService, clock)
  }

  "register" should "return error if sidecarrepo returns error" in new Setup {

    private val internalError = Errors.InternalError
    when(sidecarRepository.save(any())).thenReturn(Future.successful(Left(internalError)))
    await(sidecarService.register(RegisterRequest(sidecarId, serviceName))) shouldBe Left(internalError)
  }

  it should "return error if keyService returns error" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, now)
    when(sidecarRepository.save(sidecar)).thenReturn(Future.successful(Right(sidecar)))
    when(keyService.create(CreateKeyRequest(sidecarId))).thenReturn(Future.successful(Left(Errors.InvalidRequest)))

    await(sidecarService.register(RegisterRequest(sidecarId, serviceName))) shouldBe Left(Errors.InvalidRequest)
  }

  it should "return register result if sidecar and keys are saved" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, now)
    when(sidecarRepository.save(sidecar)).thenReturn(Future.successful(Right(sidecar)))
    private val keyResponse = CreateKeyResponse(UUID.randomUUID(), "public key", "private key")
    when(keyService.create(CreateKeyRequest(sidecarId))).thenReturn(Future.successful(Right(keyResponse)))

    await(sidecarService.register(RegisterRequest(sidecarId, serviceName))) shouldBe Right(RegisterResponse(sidecar, keyResponse))
  }

  "terminate" should "update terminated time in repo" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, now)
    when(sidecarRepository.terminate(sidecarId, now)).thenReturn(Future.successful(1))

    await(sidecarService.terminate(sidecar)) shouldBe Sidecar(sidecarId, serviceName, now, Some(now))
  }
}
