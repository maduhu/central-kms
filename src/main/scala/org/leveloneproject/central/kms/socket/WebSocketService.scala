package org.leveloneproject.central.kms.socket

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys.KeyService
import org.leveloneproject.central.kms.sidecar.SidecarActor

class WebSocketService @Inject()(keyService: KeyService) extends InputConverter with OutputConverter {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()


  private def commandExecutionFlow(sidecarActor: ActorRef): Flow[Any, Any, NotUsed] = {
    val inputFlow = Flow[Any].to(Sink.actorRef(sidecarActor, PoisonPill))

    val outputFlow = Source.actorRef[Any](100, OverflowStrategy.dropTail)
        .mapMaterializedValue(sidecarActor ! SidecarActor.Connected(_))

    Flow.fromSinkAndSource(inputFlow, outputFlow)
  }

  def sidecarFlow(): Flow[Message, Message, NotUsed] = {
    val sidecarActor = system.actorOf(SidecarActor.props(keyService))
    
    Flow[Message]
      .map(fromMessage)
      .via(commandExecutionFlow(sidecarActor))
      .map(a ⇒ toMessage(a).orNull)
  }
}
