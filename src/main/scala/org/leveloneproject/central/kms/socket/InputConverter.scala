package org.leveloneproject.central.kms.socket

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.sidecar.SidecarMessageConverter

import scala.util.{Failure, Success, Try}

trait InputConverter extends SidecarMessageConverter {

  private def isEmpty(text: String) = text == null || text.isEmpty

  private def toInput(text: String): Either[Error, Input] = {
    text match {
      case _ if isEmpty(text) ⇒ Left(Errors.ParseError)
      case _ ⇒ parseJson(text) match {
        case Failure(_) ⇒ Left(Errors.ParseError)
        case Success(value) ⇒ extractInput(value)
      }
    }
  }

  private def hasMethod(value: JValue): Boolean = {
    (value \ "method") != JNothing
  }

  private def hasResult(value: JValue): Boolean = {
    (value \ "result") != JNothing
  }

  private def extractInput(value: JValue): Either[Error, Input] = {
    Try(value match {
      case x if hasMethod(x) ⇒ Right(value.extract[SocketRequest])
      case x if hasResult(x) ⇒ Right(value.extract[SocketResponse])
      case _ ⇒ Left(Errors.InvalidRequest)
    }) match {
      case Success(x) ⇒ x
      case Failure(_) ⇒ Left(Errors.InvalidRequest)
    }
  }

  private def parseJson(text: String): Try[JValue] = {
    Try(parse(text))
  }

  def fromMessage(message: Message): Any = {
    def collect(message: Message): Either[Error, Input] = {
      message match {
        case TextMessage.Strict(text) ⇒ toInput(text)
        case _ ⇒ Left(Errors.ParseError)
      }
    }

    val input = for {
      collected ← collect(message)
      command ← toSidecarMessage(collected)
    } yield command

    input.fold(error ⇒ error, input ⇒ input)
  }
}


