package org.leveloneproject.central.kms.socket

import java.util.UUID

import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.leveloneproject.central.kms.domain.keys.KeyDomain.{KeyRequest, KeyResponse}
import org.leveloneproject.central.kms.domain.keys.KeyService
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class SocketRouteSpec extends FlatSpec with Matchers with MockitoSugar with ScalatestRouteTest {
  trait Setup {
    final val keyService: KeyService = mock[KeyService]
    final val sidecarId: UUID = UUID.randomUUID()
  }

  "socket router" should "be able to connect to websocket route" in new Setup {
    val socketRouter = new SocketRouter(keyService)
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      isWebSocketUpgrade shouldBe true
    }
  }

  it should "return error for invalid command" in new Setup{
    val parseError = RpcErrors.ParseError
    val socketRouter = new SocketRouter(keyService)
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage("test")
      wsClient.expectMessage("{\"jsonrpc\":\"2.0\",\"error\":{\"code\":%s,\"message\":\"%s\"},\"id\":null}".format(parseError.code, parseError.message))
    }
  }

  it should "return registered for register command" in new Setup {
    when(keyService.create(KeyRequest(sidecarId, "some service"))).thenReturn(Future.successful(Right(KeyResponse(sidecarId,"some service", "batchKey"))))
    val socketRouter = new SocketRouter(keyService)
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage("{\"jsonrpc\":\"2.0\",\"id\":\"some id\",\"method\":\"register\",\"params\":{\"id\":\"%s\",\"serviceName\":\"some service\"}}".format(sidecarId))
      wsClient.expectMessage("{\"jsonrpc\":\"2.0\",\"result\":{\"id\":\"%s\",\"batchKey\":\"batchKey\",\"rowKey\":\"\"},\"id\":\"some id\"}".format(sidecarId))
    }
  }

  it should "return method not allowed error when already registered" in new Setup {
    when(keyService.create(KeyRequest(sidecarId, "some service"))).thenReturn(Future.successful(Right(KeyResponse(sidecarId,"some service", "batchKey"))))
    val socketRouter = new SocketRouter(keyService)
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage("{\"jsonrpc\":\"2.0\",\"id\":\"some id\",\"method\":\"register\",\"params\":{\"id\":\"%s\",\"serviceName\":\"some service\"}}".format(sidecarId))
      wsClient.expectMessage("{\"jsonrpc\":\"2.0\",\"result\":{\"id\":\"%s\",\"batchKey\":\"batchKey\",\"rowKey\":\"\"},\"id\":\"some id\"}".format(sidecarId))
      wsClient.sendMessage("{\"jsonrpc\":\"2.0\",\"id\":\"some id\",\"method\":\"register\",\"params\":{\"id\":\"%s\",\"serviceName\":\"some service\"}}".format(sidecarId))
      wsClient.expectMessage("{\"jsonrpc\":\"2.0\",\"error\":{\"code\":%s,\"message\":\"%s\"},\"id\":\"some id\"}".format(100, "'register' method not allowed in current state"))
    }
  }

}
