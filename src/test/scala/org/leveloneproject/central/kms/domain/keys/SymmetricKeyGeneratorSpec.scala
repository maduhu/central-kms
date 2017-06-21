package org.leveloneproject.central.kms.domain.keys

import org.leveloneproject.central.kms.AwaitResult
import org.scalatest.{FlatSpec, Matchers}

class SymmetricKeyGeneratorSpec extends FlatSpec with Matchers with AwaitResult {

  trait Setup {
    val generator = new SymmetricKeyGenerator
  }

  "generate" should "generate 32 byte key" in new Setup {
    await(generator.generate()).length shouldBe 64
  }
}
