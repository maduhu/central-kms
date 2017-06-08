package org.leveloneproject.central.kms.persistance.postgres

import java.sql.SQLException
import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys._
import org.leveloneproject.central.kms.persistance.DatabaseHelper
import org.leveloneproject.central.kms.persistance.dao.KeysTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PostgresKeyStore @Inject()(repo: KeysRepo) extends KeyStore with KeysTable with DatabaseHelper {
  def create(key: Key): Future[Either[CreateError, Key]] = {
    repo.insert(key).map { _ ⇒ Right(key) }
        .recover {
          case ex: SQLException if isPrimaryKeyViolation(ex) ⇒ Left(CreateError.KeyExists(key.id))
          case _: SQLException ⇒ Left(CreateError.DatabaseFailed())
          case _: Throwable ⇒ Left(CreateError.CreateFailed())
        }
  }

  def getById(id: UUID): Future[Option[Key]] = {
    repo.getById(id)
  }
}

