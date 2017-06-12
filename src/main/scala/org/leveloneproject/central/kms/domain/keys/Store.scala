package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import org.leveloneproject.central.kms.domain._
import scala.concurrent.Future

trait Store {

  def create(key: Key): Future[Either[Error, Key]]

  def getById(id: UUID): Future[Option[Key]]
}
