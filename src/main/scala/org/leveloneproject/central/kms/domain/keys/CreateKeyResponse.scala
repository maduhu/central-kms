package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

case class CreateKeyResponse(id: UUID, publicKey: String, privateKey: String)
