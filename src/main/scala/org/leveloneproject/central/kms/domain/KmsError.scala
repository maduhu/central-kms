package org.leveloneproject.central.kms.domain

import java.util.UUID

case class KmsError(code: Int, message: String, data: Option[Any] = None)

object KmsError {
  def healthCheckDoesNotExist = KmsError(121, "Health check does not exist")


  def parseError = KmsError(-32700, "Parse error")

  def invalidRequest = KmsError(-32600, "Invalid Request")
  def methodNotFound = KmsError(-32601, "Method not found")
  def invalidParams = KmsError(-32602, "Invalid params")
  def internalError = KmsError(-32603, "Internal error")

  def sidecarExistsError(id: UUID) = KmsError(110, "Sidecar with id '%s' already exists".format(id))

  def batchExistsError(id: UUID) = KmsError(120, "Batch with id '%s' already exists".format(id))

  def methodNotAllowed(method: String) = KmsError(100, s"'$method' method not allowed in current state")

  def unregisteredSidecar(id: UUID) = KmsError(1400, "Sidecar '%s' is not registered".format(id))
}

