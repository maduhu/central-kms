package org.leveloneproject.central.kms.domain.keys

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys.KeyDomain._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KeyService @Inject()(keyGenerator: KeyGenerator, keyStore: KeyStore, verifier: Verifier) {
  def create(keyRequest: KeyRequest): Future[KeyResponse] = {
    keyGenerator.generate flatMap  { k ⇒
      keyStore.create(Key(k.id, k.publicKey)) map { _ ⇒ KeyResponse(k.id, k.privateKey) }
    }
  }

  def validate(validateRequest: ValidateRequest): Future[Either[ValidateError, ValidateResponse]] = {
    keyStore.getById(validateRequest.id).map {
      case Some(key) ⇒ verifier.verify(key.cryptoKey, validateRequest.signature, validateRequest.message)
      case None ⇒ Left(ValidateErrors.KeyNotFound)
    }
  }
}


