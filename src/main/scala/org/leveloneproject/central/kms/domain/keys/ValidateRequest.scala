package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

case class ValidateRequest(id: UUID, signature: String, message: String)
