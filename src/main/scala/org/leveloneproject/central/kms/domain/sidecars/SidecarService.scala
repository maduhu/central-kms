package org.leveloneproject.central.kms.domain.sidecars

import java.time.Clock

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys.{CreateKeyRequest, KeyService}
import org.leveloneproject.central.kms.domain.{Error, Sidecar}
import org.leveloneproject.central.kms.persistance.SidecarRepository
import org.leveloneproject.central.kms.util.FutureEither

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SidecarService @Inject()(
                                sidecarRepository: SidecarRepository,
                                keyService: KeyService,
                                clock: Clock
                              ) {

  def register(request: RegisterRequest): Future[Either[Error, RegisterResponse]] = {
    val sidecar = Sidecar(request.id, request.serviceName, clock.instant)

    for {
      s ← FutureEither(sidecarRepository.save(sidecar))
      k ← FutureEither(keyService.create(CreateKeyRequest(s.id)))
    } yield RegisterResponse(s, k)
  }

  def terminate(sidecar: Sidecar): Future[Sidecar] = {
    val now = clock.instant()
    sidecarRepository.terminate(sidecar.id, now) map (_ ⇒ sidecar.copy(terminated = Some(now)))
  }

  def active(): Future[Seq[Sidecar]] = sidecarRepository.active()
}
