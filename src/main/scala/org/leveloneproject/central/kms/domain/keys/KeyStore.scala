package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait KeyStore {
  def create(key: Key): Future[Either[CreateError, Key]]
  def getById(id: UUID): Future[Option[Key]]
}

