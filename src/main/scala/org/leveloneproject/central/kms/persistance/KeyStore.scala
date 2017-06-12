package org.leveloneproject.central.kms.persistance

import java.sql.SQLException
import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.domain.keys._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KeyStore @Inject()(keyRepository: KeyRepository) extends Store with DatabaseHelper {
  def create(key: Key): Future[Either[Error, Key]] = {
    keyRepository.insert(key).map { _ ⇒ Right(key) }
      .recover {
        case ex: SQLException if isPrimaryKeyViolation(ex) ⇒ Left(Errors.SidecarExistsError(key.id))
        case _ ⇒ Left(Errors.InternalError)
      }
  }

  def getById(id: UUID): Future[Option[Key]] = keyRepository.getById(id)
}

