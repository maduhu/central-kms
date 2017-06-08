package org.leveloneproject.central.kms.domain.keys

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys.KeyDomain._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KeyService @Inject()(keyGenerator: KeyGenerator, keyStore: KeyStore, verifier: Verifier) {
  def create(keyRequest: KeyRequest): Future[Either[CreateError, KeyResponse]] = {
    keyGenerator.generate flatMap { k ⇒
      keyStore.create(Key(keyRequest.id, keyRequest.serviceName, k.publicKey)).map {
        _.fold(e ⇒ Left(e), key ⇒ Right(KeyResponse(key.id, key.serviceName, k.privateKey)))
      }
    }
  }

  def validate(validateRequest: ValidateRequest): Future[Either[ValidateError, ValidateResponse]] = {
    keyStore.getById(validateRequest.id).map {
      case Some(key) ⇒ verifier.verify(key.cryptoKey, validateRequest.signature, validateRequest.message)
      case None ⇒ Left(ValidateErrors.KeyNotFound)
    }
  }
}


