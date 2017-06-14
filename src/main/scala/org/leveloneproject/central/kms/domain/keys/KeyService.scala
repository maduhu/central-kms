package org.leveloneproject.central.kms.domain.keys

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.persistance.KeyStore
import org.leveloneproject.central.kms.util.FutureEither

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KeyService @Inject()(keyGenerator: KeyGenerator, keyStore: KeyStore, verifier: Verifier) {
  def create(keyRequest: CreateKeyRequest): Future[Either[Error, CreateKeyResponse]] = {

    for {
      keyPair ← keyGenerator.generate()
      key ← FutureEither(keyStore.create(Key(keyRequest.id, keyPair.publicKey)))
    } yield CreateKeyResponse(key.id, keyPair.publicKey, keyPair.privateKey)
  }

  def validate(validateRequest: ValidateRequest): Future[Either[ValidateError, ValidateResponse]] = {
    keyStore.getById(validateRequest.id).map {
      case Some(key) ⇒ verifier.verify(key.cryptoKey, validateRequest.signature, validateRequest.message)
      case None ⇒ Left(ValidateErrors.KeyNotFound)
    }
  }
}


