package org.leveloneproject.central.kms.domain.keys

import java.security.KeyPairGeneratorSpi

import com.google.inject.Inject
import org.leveloneproject.central.kms.util.Bytes

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class KeyGenerator @Inject()(keyPairGenerator: KeyPairGeneratorSpi) {
  def generate(): Future[PublicPrivateKeyPair] = Future {
    val keyPair = keyPairGenerator.generateKeyPair()

    PublicPrivateKeyPair(Bytes.toHex(keyPair.getPublic.getEncoded), Bytes.toHex(keyPair.getPrivate.getEncoded))
  }
}


