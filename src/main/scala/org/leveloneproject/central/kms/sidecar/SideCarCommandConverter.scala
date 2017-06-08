package org.leveloneproject.central.kms.sidecar

import org.json4s.ext.JavaTypesSerializers
import org.json4s.{DefaultFormats, MappingException, _}
import org.leveloneproject.central.kms.domain.ErrorWithCommandId
import org.leveloneproject.central.kms.socket.{RpcErrors, RpcInput}

trait SideCarCommandConverter {

  implicit val formats = DefaultFormats ++ JavaTypesSerializers.all

  def toSideCarCommand(input: RpcInput): Either[ErrorWithCommandId, SideCarCommand] = {
    input.method match {
      case "register" ⇒ {
        try {
          Right(RegisterCommand(input.id.getOrElse(""), input.params.extract[RegisterParameters]))
        } catch {
          case _: MappingException ⇒ Left(RpcErrors.InvalidParameters(input.id.orNull))
        }
      }
      case _ ⇒ Left(RpcErrors.MethodNotFound(input.id.orNull))
    }
  }
}
