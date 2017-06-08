package org.leveloneproject.central.kms.socket

import java.util.UUID

import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage}
import akka.util.ByteString
import org.leveloneproject.central.kms.sidecar.{RegisterCommand, RegisterParameters}
import org.scalatest.{FlatSpec, Matchers}

class InputConverterSpec extends FlatSpec with Matchers {
  import RpcErrors._

  trait Setup {
    val converter = new InputConverter {}
  }

  "fromMessage" should "convert BinaryMessage to ParseError" in new Setup {
    val message = BinaryMessage.Strict(ByteString("test"))
    val result = converter.fromMessage(message)

    result shouldBe ParseError
  }

  it should "convert empty json to ParseError" in new Setup {
    val message = TextMessage.Strict("")
    val result = converter.fromMessage(message)

    result shouldBe ParseError
  }

  it should "convert invalid json to ParseError" in new Setup {
    val message = TextMessage.Strict("invalid json")
    val result = converter.fromMessage(message)

    result shouldBe ParseError
  }

  it should "convert non rpc request to InvalidRequest" in new Setup {
    val message = TextMessage.Strict("{}")
    val result = converter.fromMessage(message)

    result shouldBe InvalidRequest
  }

  it should "convert unknown method to MethodNotFound" in new Setup {
    val message = TextMessage.Strict("{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"method\":\"unknown\",\"params\":{}}")
    val result = converter.fromMessage(message)

    result shouldBe MethodNotFound("test")
  }

  it should "convert register method with bad parameters to InvalidParams" in new Setup {
    val message = TextMessage.Strict("{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"method\":\"register\",\"params\":{}}")
    val result = converter.fromMessage(message)

    result shouldBe InvalidParameters("test")
  }

  it should "convert register method with non UUID id to InvalidParams" in new Setup {
    val message = TextMessage.Strict("{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"method\":\"register\",\"params\":{\"id\":\"jfdjsfjlas\",\"serviceName\":\"value\"}}")
    val result = converter.fromMessage(message)

    result shouldBe InvalidParameters("test")
  }

  it should "convert register method to RegisterCommand" in new Setup {
    val registerId = UUID.randomUUID()
    val message = TextMessage.Strict("{\"jsonrpc\":\"2.0\",\"id\":\"test\",\"method\":\"register\",\"params\":{\"id\":\"%s\",\"serviceName\":\"value\"}}".format(registerId))
    val result = converter.fromMessage(message)

    result shouldBe RegisterCommand("test", RegisterParameters(registerId, "value"))
  }
}
