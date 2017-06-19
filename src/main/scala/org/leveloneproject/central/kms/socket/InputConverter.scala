package org.leveloneproject.central.kms.socket

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import org.json4s.ParserUtil._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.sidecar.SideCarCommandConverter

trait InputConverter extends SideCarCommandConverter {

  private def isEmpty(text: String) = text == null || text.isEmpty

  def toRpcInput(text: String): Either[Error, SocketRequest] = {
    text match {
      case _ if isEmpty(text) ⇒ Left(Errors.ParseError)
      case _ ⇒ try {
        Right(parse(text).extract[SocketRequest])
      } catch {
        case _: ParseException ⇒ Left(Errors.ParseError)
        case _: MappingException ⇒ Left(Errors.InvalidRequest)
      }
    }

  }

  def fromMessage(message: Message): Any = {
    def collect(message: Message): Either[Error, SocketRequest] = {
      message match {
        case TextMessage.Strict(text) ⇒ toRpcInput(text)
        case _ ⇒ Left(Errors.ParseError)
      }
    }

    val input = for {
      collected ← collect(message)
      command ← toSideCarCommand(collected)
    } yield command

    input.fold(error ⇒ error, input ⇒ input)
  }
}


