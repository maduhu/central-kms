package org.leveloneproject.central.kms.socket

import java.util.UUID

import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage}
import akka.util.ByteString
import org.leveloneproject.central.kms.domain.Errors._
import org.leveloneproject.central.kms.sidecar.batch.{BatchCommand, BatchParameters}
import org.leveloneproject.central.kms.sidecar.registration.{RegisterCommand, RegisterParameters}
import org.leveloneproject.central.kms.utils.MessageBuilder
import org.scalatest.{FlatSpec, Matchers}

class InputConverterSpec extends FlatSpec with Matchers with MessageBuilder {


  trait Setup {
    val converter = new InputConverter {}
  }

  "fromMessage" should "convert BinaryMessage to ParseError" in new Setup {
    val message = BinaryMessage.Strict(ByteString("test"))
    converter.fromMessage(message) shouldBe ParseError
  }

  it should "convert empty json to ParseError" in new Setup {
    val message = TextMessage.Strict("")
    converter.fromMessage(message) shouldBe ParseError
  }

  it should "convert invalid json to ParseError" in new Setup {
    val message = TextMessage.Strict("invalid json")
    converter.fromMessage(message) shouldBe ParseError
  }

  it should "convert non rpc request to InvalidRequest" in new Setup {
    val message = TextMessage.Strict("{}")
    converter.fromMessage(message) shouldBe InvalidRequest
  }

  it should "convert unknown method to MethodNotFound" in new Setup {
    val message = TextMessage.Strict("{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"method\":\"unknown\",\"params\":{}}")
    converter.fromMessage(message) shouldBe MethodNotFound("test")
  }

  it should "convert register method with bad parameters to InvalidParams" in new Setup {
    val message = TextMessage.Strict("{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"method\":\"register\",\"params\":{}}")
    converter.fromMessage(message) shouldBe InvalidParameters("test")
  }

  it should "convert register method with non UUID id to InvalidParams" in new Setup {
    val message = TextMessage.Strict("{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"method\":\"register\",\"params\":{\"id\":\"jfdjsfjlas\",\"serviceName\":\"value\"}}")
    converter.fromMessage(message) shouldBe InvalidParameters("test")
  }

  it should "convert register method to RegisterCommand" in new Setup {
    private val sidecarId = UUID.randomUUID()
    private val serviceName = "value"
    private val requestId = "test"
    val message = TextMessage.Strict(registerRequest(requestId, sidecarId = sidecarId, serviceName = serviceName))
    converter.fromMessage(message) shouldBe RegisterCommand(requestId, RegisterParameters(sidecarId, serviceName))
  }

  it should "convert batch method with no parameters to InvalidParams" in new Setup {
    val message = TextMessage.Strict("{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"method\":\"batch\",\"params\":{}}")
    converter.fromMessage(message) shouldBe InvalidParameters("test")
  }

  it should "convert batch method with invalid parameters to InvalidParams" in new Setup {
    val message = TextMessage.Strict("{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"method\":\"batch\",\"params\":{\"id\":\"%s\",\"badparametername\":\"test\"}}".format(UUID.randomUUID()))
    converter.fromMessage(message) shouldBe InvalidParameters("test")
  }

  it should "convert batch method with non UUID id to InvalidParams" in new Setup {
    val message = TextMessage.Strict("{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"method\":\"batch\",\"params\":{\"id\":\"jfdjsfjlas\",\"signature\":\"value\"}}")
    converter.fromMessage(message) shouldBe InvalidParameters("test")
  }

  it should "convert batch method to BatchCommand" in new Setup {

    private val requestId = UUID.randomUUID().toString
    private val batchId = UUID.randomUUID()
    private val signature = "some signature"
    val message = TextMessage.Strict(batchRequest(requestId, batchId, signature))

    converter.fromMessage(message) shouldBe BatchCommand(requestId, BatchParameters(batchId, signature))
  }
}
