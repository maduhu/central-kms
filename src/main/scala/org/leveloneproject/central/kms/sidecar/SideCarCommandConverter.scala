package org.leveloneproject.central.kms.sidecar

import org.json4s.ext.JavaTypesSerializers
import org.json4s.{DefaultFormats, MappingException, _}
import org.leveloneproject.central.kms.domain.{ErrorWithCommandId, Errors}
import org.leveloneproject.central.kms.sidecar.batch._
import org.leveloneproject.central.kms.sidecar.registration._
import org.leveloneproject.central.kms.socket.RpcRequest

trait SideCarCommandConverter {

  implicit val formats: Formats = DefaultFormats ++ JavaTypesSerializers.all

  def toSideCarCommand(input: RpcRequest): Either[ErrorWithCommandId, SideCarCommand] = {
    try {
      input.method match {
        case "register" ⇒ Right(RegisterCommand(input.id.getOrElse(""), input.params.extract[RegisterParameters]))
        case "batch" ⇒ Right(BatchCommand(input.id.getOrElse(""), input.params.extract[BatchParameters]))
        case _ ⇒ Left(Errors.MethodNotFound(input.id.orNull))
      }
    } catch {
      case _: MappingException ⇒ Left(Errors.InvalidParameters(input.id.orNull))
    }
  }
}
