package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import com.google.inject.Inject

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class KeyService @Inject()(keyGenerator: KeyGenerator) {
  def create(keyRequest: KeyRequest): Future[KeyResponse] = {
    keyGenerator.generate map(s ⇒ KeyResponse(s.id, s.privateKey))
  }
}

case class KeyRequest()

case class KeyResponse(id: UUID, privateKey: String)
