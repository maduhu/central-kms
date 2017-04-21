package org.leveloneproject.central.kms.domain.keys

import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import net.i2p.crypto.eddsa.EdDSAPublicKey
import org.leveloneproject.central.kms.util.Bytes
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object KeyDomain extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val RegistrationResponseFormat: RootJsonFormat[RegistrationResponse] = jsonFormat2(RegistrationResponse)
  implicit val RegistrationRequestFormat: RootJsonFormat[RegistrationRequest] = jsonFormat0(RegistrationRequest)
  implicit val ValidationRequestFormat: RootJsonFormat[ValidationRequest] = jsonFormat2(ValidationRequest)
  implicit val ValidateErrorFormat: RootJsonFormat[ValidateError] = jsonFormat1(ValidateError)
  implicit val ValidateResponseFormat: RootJsonFormat[ValidateResponse] = jsonFormat1(ValidateResponse)

  case class RegistrationRequest()

  case class RegistrationResponse(
    id: String,
    privateKey: String
  )

  case class ValidationRequest(signature: String, message: String)

  case class Key(id: UUID, publicKey: String) {
    lazy val cryptoKey: PublicKey = new EdDSAPublicKey(new X509EncodedKeySpec(Bytes.fromHex(publicKey)))
  }

  case class KeyRequest()


  case class KeyResponse(id: UUID, privateKey: String)

  case class ValidateRequest(id: UUID, signature: String, message: String)

  case class ValidateResponse(success: String)

  object ValidateResponses {
    final val Success = ValidateResponse("OK")
  }

  case class ValidateError(error: String)

  object ValidateErrors {
    final val KeyNotFound = ValidateError("Key not found")
    final val InvalidSignature = ValidateError("Invalid signature")
  }

  case class PublicPrivateKeyPair(id: UUID, publicKey: String, privateKey: String)
}
