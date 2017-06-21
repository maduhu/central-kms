package org.leveloneproject.central.kms.domain.keys

import java.security.KeyPairGeneratorSpi

import com.google.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AsymmetricKeyGenerator @Inject()(keyPairGenerator: KeyPairGeneratorSpi) {

  import org.leveloneproject.central.kms.util.Bytes.Hex

  def generate(): Future[PublicPrivateKeyPair] = Future {
    val keyPair = keyPairGenerator.generateKeyPair()

    PublicPrivateKeyPair(keyPair.getPublic.getEncoded.toHex, keyPair.getPrivate.getEncoded.toHex)
  }
}
