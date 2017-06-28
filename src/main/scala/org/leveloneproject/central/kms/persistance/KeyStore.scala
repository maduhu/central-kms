package org.leveloneproject.central.kms.persistance

import java.sql.SQLException
import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.keys._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KeyStore @Inject()(keyRepository: KeyRepository) extends Store with DatabaseHelper {
  def create(key: Key): Future[Either[KmsError, Key]] = {
    keyRepository.insert(key).map { _ ⇒ Right(key) }
      .recover {
        case ex: SQLException if isPrimaryKeyViolation(ex) ⇒ Left(KmsError.sidecarExistsError(key.id))
        case _ ⇒ Left(KmsError.internalError)
      }
  }

  def getById(id: UUID): Future[Option[Key]] = keyRepository.getById(id)
}

