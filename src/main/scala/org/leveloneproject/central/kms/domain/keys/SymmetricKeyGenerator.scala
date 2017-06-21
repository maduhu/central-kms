package org.leveloneproject.central.kms.domain.keys

import javax.crypto.KeyGenerator
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SymmetricKeyGenerator {
  import org.leveloneproject.central.kms.util.Bytes.Hex

  private val keySize = 256
  private val algorithm = "AES"

  def generate(): Future[String] = Future {
    val generator = KeyGenerator.getInstance(algorithm)
    generator.init(256)
    generator.generateKey().getEncoded.toHex
  }
}
