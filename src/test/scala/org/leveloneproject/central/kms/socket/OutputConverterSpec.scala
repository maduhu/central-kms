package org.leveloneproject.central.kms.socket

import akka.http.scaladsl.model.ws.TextMessage
import org.leveloneproject.central.kms.domain.{CommandResponse, Error, ErrorWithCommandId}
import org.scalatest.{FlatSpec, Matchers}

class OutputConverterSpec extends FlatSpec with Matchers {

  trait Setup {
    val converter = new OutputConverter {}
  }

  "toMessage" should "convert input error to rpc error response" in new Setup {
    val error = Error(14, "some message")

    val result = converter.toMessage(error)

    result shouldBe Some(TextMessage.Strict("{\"jsonrpc\":\"2.0\",\"error\":{\"code\":14,\"message\":\"some message\"},\"id\":null}"))
  }

  it should "convert ErrorWithCommandId to rpc error response" in new Setup {
    val error = ErrorWithCommandId(Error(100, "some message"), "commandid")

    val result = converter.toMessage(error)

    result shouldBe Some(TextMessage.Strict("{\"jsonrpc\":\"2.0\",\"error\":{\"code\":100,\"message\":\"some message\"},\"id\":\"commandid\"}"))
  }

  it should "convert command response to rpc response" in new Setup {
    val r = SomeResponse("some id", 100)
    val response = CommandResponse(r, "commandid")

    val result = converter.toMessage(response)
    result shouldBe Some(TextMessage.Strict("{\"jsonrpc\":\"2.0\",\"result\":{\"id\":\"some id\",\"value\":100},\"id\":\"commandid\"}"))
  }

  it should "return None for other Types" in new Setup {
    converter.toMessage(SomeResponse("fjdkfjdsl", 1000)) shouldBe(None)
    converter.toMessage("some string") shouldBe(None)
  }
}

case class SomeResponse(id: String, value: Int)