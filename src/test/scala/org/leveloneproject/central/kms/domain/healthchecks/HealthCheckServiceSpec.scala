package org.leveloneproject.central.kms.domain.healthchecks

import java.time.{Clock, Instant}
import java.util.UUID

import akka.testkit.TestProbe
import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.Errors
import org.leveloneproject.central.kms.domain.healthchecks.HealthCheckLevel.Ping
import org.leveloneproject.central.kms.domain.healthchecks.HealthCheckStatus.Pending
import org.leveloneproject.central.kms.domain.sidecars.SidecarList
import org.leveloneproject.central.kms.persistance.HealthCheckRepository
import org.leveloneproject.central.kms.util.IdGenerator
import org.leveloneproject.central.kms.utils.AkkaSpec
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class HealthCheckServiceSpec extends FlatSpec with AkkaSpec with Matchers with MockitoSugar with AwaitResult {

  trait Setup {
    final val healthCheckRepo: HealthCheckRepository = mock[HealthCheckRepository]
    final val clock: Clock = mock[Clock]
    final val now: Instant = Instant.now()
    when(clock.instant()).thenReturn(now)
    final val sidecarList: SidecarList = mock[SidecarList]

    final val healthCheckId: UUID = UUID.randomUUID()

    val service: HealthCheckService = new HealthCheckService(healthCheckRepo, clock, sidecarList) with IdGenerator {
      override def newId(): UUID = healthCheckId
    }

    final val sidecarId: UUID = UUID.randomUUID()
  }

  "create" should "create, save and return new HealthCheck" in new Setup {
    val request = CreateHealthCheckRequest(sidecarId, Ping)

    private val check = HealthCheck(healthCheckId, sidecarId, Ping, now, Pending, None, None)

    private val sidecarProbe = TestProbe()
    when(sidecarList.actorById(sidecarId)).thenReturn(Future.successful(Right(sidecarProbe.ref)))
    when(healthCheckRepo.insert(check)).thenReturn(Future.successful((): Unit))

    await(service.create(request)) shouldBe Right(check)

    verify(healthCheckRepo, times(1)).insert(check)
    sidecarProbe.expectMsg(check)
  }

  it should "return error if sidecar not found" in new Setup {
    val request = CreateHealthCheckRequest(sidecarId, Ping)

    private val error = Errors.UnregisteredSidecar(sidecarId)
    when(sidecarList.actorById(sidecarId)).thenReturn(Future.successful(Left(error)))

    await(service.create(request)) shouldBe Left(error)
  }
}
