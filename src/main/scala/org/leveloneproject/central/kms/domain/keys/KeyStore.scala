package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import org.leveloneproject.central.kms.domain.keys.KeyDomain.Key

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait KeyStore {
  def create(key: Key): Future[Key]
  def getById(id: UUID): Future[Option[Key]]
}
