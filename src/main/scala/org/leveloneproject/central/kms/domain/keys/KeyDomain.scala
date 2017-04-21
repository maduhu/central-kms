package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object KeyDomain extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val RegistrationResponseFormat: RootJsonFormat[RegistrationResponse] = jsonFormat2(RegistrationResponse)
  implicit val RegistrationRequestFormat: RootJsonFormat[RegistrationRequest] = jsonFormat0(RegistrationRequest)

  case class RegistrationRequest()

  case class RegistrationResponse(
    id: String,
    privateKey: String
  )

  case class Key(id: UUID, publicKey: String)
}
