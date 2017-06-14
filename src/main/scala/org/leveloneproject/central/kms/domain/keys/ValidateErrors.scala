package org.leveloneproject.central.kms.domain.keys

object ValidateErrors {
  final val KeyNotFound = ValidateError("Key not found")
  final val InvalidSignature = ValidateError("Invalid signature")
}
