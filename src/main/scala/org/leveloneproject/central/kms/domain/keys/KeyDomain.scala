package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

object KeyDomain {

  case class RegistrationRequest()

  case class RegistrationResponse(
    id: String,
    privateKey: String
  )

  case class ValidationRequest(signature: String, message: String)

  case class KeyRequest(id: UUID, serviceName: String)

  case class KeyResponse(id: UUID, serviceName: String, privateKey: String)

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

  case class PublicPrivateKeyPair(publicKey: String, privateKey: String)
}
