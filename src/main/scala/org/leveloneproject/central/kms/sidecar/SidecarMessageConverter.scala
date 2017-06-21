package org.leveloneproject.central.kms.sidecar

import org.json4s._
import org.leveloneproject.central.kms.domain.{ErrorWithCommandId, Errors}
import org.leveloneproject.central.kms.sidecar.batch._
import org.leveloneproject.central.kms.sidecar.registration._
import org.leveloneproject.central.kms.socket.{Input, SocketRequest, SocketResponse}
import org.leveloneproject.central.kms.util.JsonFormats

trait SidecarMessageConverter extends JsonFormats {

  def toSidecarMessage(input: Input): Either[ErrorWithCommandId, SidecarMessage] = {
    input match {
      case r: SocketRequest ⇒
        try
          r.method match {
            case "register" ⇒ Right(RegisterCommand(r.id, r.params.extract[RegisterParameters]))
            case "batch" ⇒ Right(BatchCommand(r.id, r.params.extract[BatchParameters]))
            case _ ⇒ Left(Errors.MethodNotFound(r.id))
          }
        catch {
          case _: MappingException ⇒ Left(Errors.InvalidParameters(r.id))
        }

      case r: SocketResponse ⇒
        Right(CompleteRequest(r.id, r.result, r.error))
    }

  }
}
