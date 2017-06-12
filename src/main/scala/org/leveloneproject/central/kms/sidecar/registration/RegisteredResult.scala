package org.leveloneproject.central.kms.sidecar.registration

import java.util.UUID

case class RegisteredResult(id: UUID, batchKey: String, rowKey: String)
