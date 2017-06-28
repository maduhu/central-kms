package org.leveloneproject.central.kms.persistance

import java.sql.SQLException
import java.util.UUID

import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.sidecars.{Sidecar, SidecarStatus}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SidecarRepository extends SidecarsTable with DatabaseHelper {
  this: DbProfile ⇒

  val dbProvider: DbProvider

  import profile.api._

  private lazy val db = dbProvider.db

  def insert(sidecar: Sidecar): Future[Either[KmsError, Sidecar]] = {
    db.run(sidecars += sidecar).map { _ ⇒ Right(sidecar) }
      .recover {
        case ex: SQLException if isPrimaryKeyViolation(ex) ⇒ Left(KmsError.sidecarExistsError(sidecar.id))
        case _: Throwable ⇒ Left(KmsError.internalError)
      }
  }

  def updateStatus(id: UUID, sidecarStatus: SidecarStatus): Future[Either[KmsError, Int]] = {
    db.run(sidecars.filter(_.id === id).map(s ⇒ s.status).update(sidecarStatus)).map(Right(_))
      .recover {
        case _: Throwable ⇒ Left(KmsError.internalError)
      }
  }
}
