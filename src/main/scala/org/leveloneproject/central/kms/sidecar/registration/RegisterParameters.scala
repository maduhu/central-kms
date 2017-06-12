package org.leveloneproject.central.kms.sidecar.registration

import java.util.UUID

import org.leveloneproject.central.kms.domain.keys.KeyDomain.KeyRequest

case class RegisterParameters(id: UUID, serviceName: String) {
  def toKeyRequest(): KeyRequest = KeyRequest(id, serviceName)
}
