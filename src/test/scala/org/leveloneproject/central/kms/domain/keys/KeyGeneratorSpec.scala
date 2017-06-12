package org.leveloneproject.central.kms.domain.keys

import java.security.{KeyPair, KeyPairGeneratorSpi, PrivateKey, PublicKey}

import net.i2p.crypto.eddsa.KeyPairGenerator
import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.keys.KeyDomain.PublicPrivateKeyPair
import org.leveloneproject.central.kms.util.Bytes
import org.mockito.Mockito._
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar

class KeyGeneratorSpec extends FlatSpec with MockitoSugar with AwaitResult {
  private val privateKeyString = "00112233445566778899"
  private val publicKeyString = "AABBCCDDEEFF"

  trait Setup {
    val keyPairGenerator: KeyPairGeneratorSpi = mock[KeyPairGeneratorSpi]
    val keyGenerator = new KeyGenerator(keyPairGenerator)
  }

  "generate" should "generate pair" in new Setup {
    private val privateKey = mock[PrivateKey]
    when(privateKey.getEncoded).thenReturn(Bytes.fromHex(privateKeyString))
    private val publicKey = mock[PublicKey]
    when(publicKey.getEncoded).thenReturn(Bytes.fromHex(publicKeyString))
    val keyPair = new KeyPair(publicKey, privateKey)
    when(keyPairGenerator.generateKeyPair).thenReturn(keyPair)

    private val result = await(keyGenerator.generate())
    assert(result.privateKey == privateKeyString)
    assert(result.publicKey == publicKeyString)
  }

  it should "generate different keys on successive calls" in new Setup {
    val keyGen = new KeyGenerator(new KeyPairGenerator)

    val result1: PublicPrivateKeyPair = await(keyGen.generate())
    val result2: PublicPrivateKeyPair = await(keyGen.generate())
    assert(result1.privateKey != result2.privateKey)
    assert(result1.publicKey != result2.publicKey)

  }
}
