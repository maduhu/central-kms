package org.leveloneproject.central.kms.domain.keys

import com.google.inject.Inject
import org.leveloneproject.central.kms.crypto._
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.persistance.KeyStore
import org.leveloneproject.central.kms.util.FutureEither

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KeyService @Inject()(
                            asymmetricKeyGenerator: AsymmetricKeyGenerator,
                            symmetricKeyGenerator: SymmetricKeyGenerator,
                            keyStore: KeyStore,
                            verifier: AsymmetricVerifier) {
  def create(keyRequest: CreateKeyRequest): Future[Either[KmsError, CreateKeyResponse]] = {

    for {
      keyPair ← asymmetricKeyGenerator.generate()
      symmetricKey ← symmetricKeyGenerator.generate()
      key ← FutureEither(keyStore.create(Key(keyRequest.id, keyPair.publicKey)))
    } yield CreateKeyResponse(key.id, keyPair.publicKey, keyPair.privateKey, symmetricKey)
  }

  def validate(validateRequest: ValidateRequest): Future[Either[VerificationError, VerificationResult]] = {
    keyStore.getById(validateRequest.id).map {
      case Some(key) ⇒ verifier.verify(key.publicKey, validateRequest.signature, validateRequest.message)
      case None ⇒ Left(VerificationError.KeyNotFound)
    }
  }
}


