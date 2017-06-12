package org.leveloneproject.central.kms.socket

import java.time.Instant
import java.util.UUID

import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.leveloneproject.central.kms.domain.batches.{BatchService, CreateRequest}
import org.leveloneproject.central.kms.domain.keys.KeyDomain.{KeyRequest, KeyResponse}
import org.leveloneproject.central.kms.domain.keys.KeyService
import org.leveloneproject.central.kms.domain.{Batch, Errors}
import org.leveloneproject.central.kms.utils.MessageBuilder
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class SocketRouteSpec extends FlatSpec with Matchers with MockitoSugar with ScalatestRouteTest with MessageBuilder {
  trait Setup {
    final val keyService: KeyService = mock[KeyService]
    final val batchService: BatchService = mock[BatchService]
    final val sidecarId: UUID = UUID.randomUUID()
    final val serviceName: String = "some service"
    final val batchKey: String = "batch key"

    def setupRegistration(): Unit = {
      when(keyService.create(KeyRequest(sidecarId, serviceName))).thenReturn(Future.successful(Right(KeyResponse(sidecarId,serviceName, batchKey))))
    }
  }

  "socket router" should "be able to connect to websocket route" in new Setup {
    val socketRouter = new SocketRouter(keyService, batchService)
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      isWebSocketUpgrade shouldBe true
    }
  }

  it should "return error for invalid command" in new Setup{
    val parseError = Errors.ParseError
    val socketRouter = new SocketRouter(keyService, batchService)
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage("test")
      wsClient.expectMessage("{\"jsonrpc\":\"2.0\",\"error\":{\"code\":%s,\"message\":\"%s\"},\"id\":null}".format(parseError.code, parseError.message))
    }
  }

  it should "return registered for register command" in new Setup {
    setupRegistration()
    private val requestId = "test"
    val socketRouter = new SocketRouter(keyService, batchService)
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage(registerRequest(requestId, sidecarId, serviceName))
      wsClient.expectMessage(registerResponse(requestId, sidecarId, batchKey))
    }
  }

  it should "return method not allowed error when already registered" in new Setup {
    setupRegistration()
    val socketRouter = new SocketRouter(keyService, batchService)
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage(registerRequest("test", sidecarId, serviceName))
      wsClient.expectMessage(registerResponse("test", sidecarId, batchKey))
      wsClient.sendMessage(registerRequest("test2"))
      wsClient.expectMessage("{\"jsonrpc\":\"2.0\",\"error\":{\"code\":%s,\"message\":\"%s\"},\"id\":\"test2\"}".format(100, "'register' method not allowed in current state"))
    }
  }

  it should "return batch id when registered sidecar issues batch" in new Setup {
    setupRegistration()
    private val batchId = UUID.randomUUID()
    private val signature = "some signature"
    val socketRouter = new SocketRouter(keyService, batchService)
    when(batchService.create(CreateRequest(sidecarId, batchId, signature))).thenReturn(Future.successful(Right(Batch(batchId, sidecarId, signature, Instant.now()))))
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage(registerRequest("register1", sidecarId, serviceName))
      wsClient.expectMessage(registerResponse("register1", sidecarId, batchKey))
      wsClient.sendMessage(batchRequest("batch1",batchId,signature))
      wsClient.expectMessage(batchResponse("batch1",batchId))
    }
  }

}
