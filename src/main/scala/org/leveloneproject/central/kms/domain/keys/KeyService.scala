package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys.KeyDomain.Key
import org.leveloneproject.central.kms.persistance.KeyStore

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class KeyService @Inject()(keyGenerator: KeyGenerator, keyStore: KeyStore) {
  def create(keyRequest: KeyRequest): Future[KeyResponse] = {
    keyGenerator.generate flatMap  { k ⇒
      keyStore.create(Key(k.id, k.publicKey)) map { _ ⇒ KeyResponse(k.id, k.privateKey) }
    }
  }
}

case class KeyRequest()

case class KeyResponse(id: UUID, privateKey: String)


