package org.leveloneproject.central.kms.crypto

import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.util.Bytes._
import org.scalatest.{FlatSpec, Matchers}

class TweetNaClKeysSpec extends FlatSpec with Matchers with AwaitResult {

  trait Setup {
    val generator = new TweetNaClKeys
  }

  trait VerifierSetup extends Setup {
    val key: PublicPrivateKeyPair = await(generator.generate())
    val publicKey: String = key.publicKey
    val privateKey: String = key.privateKey

    val message = "Test Message"
    val signature: String = TweetNaCl.crypto_sign(message.fromUtf8, privateKey.fromHex).toHex
  }

  "generate" should "create right sized keys" in new Setup {
    private val result = await(generator.generate())
    result.publicKey.length shouldBe 64
    result.privateKey.length shouldBe 128
  }

  "verify" should "return true if signature is generated from privateKey" in new VerifierSetup {
    generator.verify(publicKey, signature, message) shouldBe Right(VerificationResult.Success)
  }

  it should "return false if message differs from signature" in new VerifierSetup {
    generator.verify(publicKey, signature, message + " ") shouldBe Left(VerificationError.InvalidSignature)
  }

  it should "return false if signature differs from message" in new VerifierSetup {
    generator.verify(publicKey, signature.replace('A', 'B'), message) shouldBe Left(VerificationError.InvalidSignature)
  }

  it should "be able to verify many times" in new VerifierSetup {
    generator.verify(publicKey, signature, message) shouldBe Right(VerificationResult.Success)
    generator.verify(publicKey, signature, message + " ") shouldBe Left(VerificationError.InvalidSignature)
    generator.verify(publicKey, signature.replace('A', 'B'), message) shouldBe Left(VerificationError.InvalidSignature)
    generator.verify(publicKey, signature, message) shouldBe Right(VerificationResult.Success)
  }

  it should "verify known signatures" in new Setup {

    // Data from http://ed25519.cr.yp.to/python/sign.input
    //noinspection SpellCheckingInspection
    val testData = "ab6f7aee6a0837b334ba5eb1b2ad7fcecfab7e323cab187fe2e0a95d80eff1325b96dca497875bf9664c5e75facf3f9bc54bae913d66ca15ee85f1491ca24d2c:5b96dca497875bf9664c5e75facf3f9bc54bae913d66ca15ee85f1491ca24d2c:8171456f8b907189b1d779e26bc5afbb08c67a:73bca64e9dd0db88138eedfafcea8f5436cfb74bfb0e7733cf349baa0c49775c56d5934e1d38e36f39b7c5beb0a836510c45126f8ec4b6810519905b0ca07c098171456f8b907189b1d779e26bc5afbb08c67a:"

    val splits: Array[String] = testData.split(':')
    val privateKey: Array[Byte] = splits(0).fromHex
    val publicKey: Array[Byte] = splits(1).fromHex
    val message: Array[Byte] = splits(2).fromHex
    val sig: Array[Byte] = splits(3).fromHex

    generator.verify(publicKey, sig, message) shouldBe Right(VerificationResult.Success)
  }

}
