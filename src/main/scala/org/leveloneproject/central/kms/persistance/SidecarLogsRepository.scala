package org.leveloneproject.central.kms.persistance

import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.sidecars.SidecarLog

import scala.concurrent.{ExecutionContext, Future}

trait SidecarLogsRepository extends SidecarLogsTable with DatabaseHelper {
  this: DbProfile ⇒

  val dbProvider: DbProvider

  import profile.api._

  private lazy val db = dbProvider.db

  def save(log: SidecarLog)(implicit ec: ExecutionContext): Future[Either[KmsError, SidecarLog]] = {
    db.run(sidecarLogs += log).map { _ ⇒ Right(log) }
      .recover {
        case _: Throwable ⇒ Left(KmsError.internalError)
      }
  }
}
