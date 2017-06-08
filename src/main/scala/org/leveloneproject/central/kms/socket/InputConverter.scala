package org.leveloneproject.central.kms.socket

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import org.json4s.ParserUtil._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.{write ⇒ swrite}
import org.leveloneproject.central.kms.domain
import org.leveloneproject.central.kms.sidecar.SideCarCommandConverter

trait InputConverter extends SideCarCommandConverter {

  import RpcErrors._

  private def isEmpty(text: String) = text == null || text.isEmpty

  def toRpcInput(text: String): Either[domain.Error, RpcInput] = {
    text match {
      case _ if isEmpty(text) ⇒ Left(ParseError)
      case _ ⇒ try {
        Right(parse(text).extract[RpcInput])
      } catch {
        case _: ParseException ⇒ Left(ParseError)
        case _: MappingException ⇒ Left(InvalidRequest)
      }
    }

  }

  def fromMessage(message: Message): Any = {
    def collect(message: Message): Either[domain.Error, RpcInput] = {
      message match {
        case TextMessage.Strict(text) ⇒ toRpcInput(text)
        case _ ⇒ Left(ParseError)
      }
    }

    val input = for {
      collected ← collect(message)
      command ← toSideCarCommand(collected)
    } yield command

    input.fold(error ⇒ error, input ⇒ input)
  }
}


