package org.leveloneproject.central.kms.socket

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.sidecar.SidecarMessageParser
import org.leveloneproject.central.kms.util.JsonDeserializer
import org.slf4j.LoggerFactory

trait InputConverter extends SidecarMessageParser with JsonDeserializer {

  private val log = LoggerFactory.getLogger(classOf[InputConverter])
  private def toJsonMessage(text: String): Either[KmsError, JsonMessage] = {
    if (text.isEmpty) Left(KmsError.parseError)
    else {
      log.info("Received message={}", text)
      deserializeSafe[JsonMessage](text)
    }
  }

  def fromMessage(message: Message): AnyRef = {
    def collect(message: Message): Either[KmsError, JsonMessage] = {
      message match {
        case TextMessage.Strict(text) ⇒ toJsonMessage(text)
        case _ ⇒ Left(KmsError.parseError)
      }
    }

    val input = for {
      collected ← collect(message)
      command ← parse(collected)
    } yield command


    input.fold(error ⇒ error, input ⇒ input)
  }
}


