package org.leveloneproject.central.kms.domain.keys

import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.UUID

import net.i2p.crypto.eddsa.EdDSAPublicKey
import org.leveloneproject.central.kms.util.Bytes

case class Key(id: UUID, publicKey: String) {
  lazy val cryptoKey: PublicKey = new EdDSAPublicKey(new X509EncodedKeySpec(Bytes.fromHex(publicKey)))
}
