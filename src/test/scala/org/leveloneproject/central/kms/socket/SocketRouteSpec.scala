package org.leveloneproject.central.kms.socket

import java.time.Instant
import java.util.UUID

import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.leveloneproject.central.kms.domain.Errors
import org.leveloneproject.central.kms.domain.batches.Batch
import org.leveloneproject.central.kms.domain.keys.CreateKeyResponse
import org.leveloneproject.central.kms.domain.sidecars.{RegisterResponse, Sidecar}
import org.leveloneproject.central.kms.sidecar.SidecarSupport
import org.leveloneproject.central.kms.sidecar.batch.BatchParameters
import org.leveloneproject.central.kms.utils.MessageBuilder
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class SocketRouteSpec extends FlatSpec with Matchers with MockitoSugar with ScalatestRouteTest with MessageBuilder {

  trait Setup {
    final val sidecarSupport: SidecarSupport = mock[SidecarSupport]
    final val webSocketService: WebSocketService = new WebSocketService(sidecarSupport)
    final val sidecarId: UUID = UUID.randomUUID()
    final val serviceName: String = "some service"
    final val publicKey: String = "public key"
    final val privateKey: String = "batch key"
    final val rowKey: String = "row key"

    def setupRegistration(): Unit = {
      val keyResponse = CreateKeyResponse(sidecarId, publicKey, privateKey, rowKey)
      val sidecar = Sidecar(sidecarId, serviceName, Instant.now())
      when(sidecarSupport.registerSidecar(any(), any())).thenReturn(Future.successful(Right(RegisterResponse(sidecar, keyResponse))))
    }
  }

  "socket router" should "be able to connect to websocket route" in new Setup {
    val socketRouter = new SocketRouter(webSocketService)
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      isWebSocketUpgrade shouldBe true
    }
  }

  it should "return error for invalid command" in new Setup {
    val parseError = Errors.ParseError
    val socketRouter = new SocketRouter(webSocketService)
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage("test")
      wsClient.expectMessage("{\"jsonrpc\":\"2.0\",\"error\":{\"code\":%s,\"message\":\"%s\"},\"id\":null}".format(parseError.code, parseError.message))
    }
  }

  it should "return registered for register command" in new Setup {
    setupRegistration()
    private val requestId = "test"
    val socketRouter = new SocketRouter(webSocketService)
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage(registerRequest(requestId, sidecarId, serviceName))
      wsClient.expectMessage(registerResponse(requestId, sidecarId, privateKey, rowKey))
    }
  }

  it should "return method not allowed error when already registered" in new Setup {
    setupRegistration()
    val socketRouter = new SocketRouter(webSocketService)
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage(registerRequest("test", sidecarId, serviceName))
      wsClient.expectMessage(registerResponse("test", sidecarId, privateKey, rowKey))
      wsClient.sendMessage(registerRequest("test2"))
      wsClient.expectMessage("{\"jsonrpc\":\"2.0\",\"error\":{\"code\":%s,\"message\":\"%s\"},\"id\":\"test2\"}".format(100, "'register' method not allowed in current state"))
    }
  }

  it should "return batch id when registered sidecar issues batch" in new Setup {
    setupRegistration()
    private val batchId = UUID.randomUUID()
    private val signature = "some signature"
    val socketRouter = new SocketRouter(webSocketService)
    when(sidecarSupport.createBatch(sidecarId, BatchParameters(batchId, signature))).thenReturn(Future.successful(Right(Batch(batchId, sidecarId, signature, Instant.now()))))
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage(registerRequest("register1", sidecarId, serviceName))
      wsClient.expectMessage(registerResponse("register1", sidecarId, privateKey, rowKey))
      wsClient.sendMessage(batchRequest("batch1", batchId, signature))
      wsClient.expectMessage(batchResponse("batch1", batchId))
    }
  }
}
