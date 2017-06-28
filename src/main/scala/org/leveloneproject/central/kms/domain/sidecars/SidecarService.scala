package org.leveloneproject.central.kms.domain.sidecars

import java.time.Clock
import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys.{CreateKeyRequest, KeyService}
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.persistance.{SidecarLogsRepository, SidecarRepository}
import org.leveloneproject.central.kms.util.{ChallengeGenerator, FutureEither, IdGenerator}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SidecarService @Inject()(
                                sidecarRepository: SidecarRepository,
                                keyService: KeyService,
                                clock: Clock,
                                sidecarList: SidecarList,
                                sidecarLogsRepository: SidecarLogsRepository
                              ) extends ChallengeGenerator with IdGenerator {

  def register(request: RegisterRequest): Future[Either[KmsError, RegisterResponse]] = {
    val status = SidecarStatus.Challenged
    val sidecar = Sidecar(request.id, request.serviceName, status, newChallenge())

    for {
      s ← FutureEither(sidecarRepository.insert(sidecar))
      k ← FutureEither(keyService.create(CreateKeyRequest(s.id)))
      _ ← FutureEither(logStatusChange(sidecar.id, status))
    } yield RegisterResponse(s, k)
  }

  def challengeAccepted(sidecarWithActor: SidecarAndActor): Future[Either[KmsError, SidecarAndActor]] = {
    FutureEither(updateStatus(sidecarWithActor.sidecar, SidecarStatus.Registered)).map { sidecar ⇒
      val n = sidecarWithActor.copy(sidecar = sidecar)
      sidecarList.register(n)
      n
    }
  }

  def terminate(sidecar: Sidecar): Future[Either[KmsError, Sidecar]] = {
    for {
      updated ← updateStatus(sidecar, SidecarStatus.Terminated)
      _ ← Future.successful(sidecarList.unregister(sidecar.id))
    } yield Right(updated)
  }

  def active(): Future[Seq[ApiSidecar]] = sidecarList.current().map(_.map(s ⇒ ApiSidecar(s.id, s.serviceName, s.status)))

  private def updateStatus(sidecar: Sidecar, newStatus: SidecarStatus): FutureEither[KmsError, Sidecar] = {
    val updated = sidecar.copy(status = newStatus)
    for {
      _ ← logStatusChange(sidecar.id, newStatus)
      _ ← sidecarRepository.updateStatus(sidecar.id, newStatus)
    } yield Right(updated)
  }

  private def logStatusChange(sidecarId: UUID, sidecarStatus: SidecarStatus, message: Option[String] = None): Future[Either[KmsError, SidecarLog]] = {
    sidecarLogsRepository.save(SidecarLog(newId(), sidecarId, clock.instant(), sidecarStatus, message))
  }
}
