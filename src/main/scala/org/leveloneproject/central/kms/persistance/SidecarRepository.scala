package org.leveloneproject.central.kms.persistance

import java.sql.SQLException
import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.domain.sidecars.Sidecar
import org.leveloneproject.central.kms.domain.{Error, Errors}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SidecarRepository extends SidecarsTable with DatabaseHelper {
  this: DbProfile ⇒

  val dbProvider: DbProvider
  import profile.api._

  private lazy val db = dbProvider.db

  def save(sidecar: Sidecar): Future[Either[Error, Sidecar]] = {
    db.run(sidecars += sidecar).map { _ ⇒ Right(sidecar) }
      .recover {
        case ex: SQLException if isPrimaryKeyViolation(ex) ⇒ Left(Errors.SidecarExistsError(sidecar.id))
        case _: Throwable ⇒ Left(Errors.InternalError)
      }
  }

  def terminate(id: UUID, timestamp: Instant): Future[Int] = {
    db.run(sidecars.filter(_.id === id).map(s ⇒ s.terminated).update(Some(timestamp)))
  }

  def active(): Future[Seq[Sidecar]] = {
    db.run(sidecars.filter(_.terminated.isEmpty).result)
  }
}
