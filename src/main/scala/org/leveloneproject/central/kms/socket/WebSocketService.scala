package org.leveloneproject.central.kms.socket

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.Message
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import com.google.inject.Inject
import org.leveloneproject.central.kms.sidecar._

class WebSocketService @Inject()(sidecarSupport: SidecarSupport)
                                (implicit val system: ActorSystem, implicit val materializer: ActorMaterializer) extends InputConverter with OutputConverter {

  private def commandExecutionFlow(sidecarActor: ActorRef): Flow[AnyRef, AnyRef, NotUsed] = {
    val inputFlow = Flow[AnyRef].to(Sink.actorRef(sidecarActor, Disconnect))

    val outputFlow = Source.actorRef[AnyRef](100, OverflowStrategy.dropTail)
      .mapMaterializedValue(sidecarActor ! Connected(_))

    Flow.fromSinkAndSource(inputFlow, outputFlow)
  }

  def sidecarFlow(): Flow[Message, Message, NotUsed] = {
    val sidecarActor = system.actorOf(SidecarActor.props(sidecarSupport))

    Flow[Message]
      .map(fromMessage)
      .via(commandExecutionFlow(sidecarActor))
      .map(a ⇒ toMessage(a).orNull)
  }
}
