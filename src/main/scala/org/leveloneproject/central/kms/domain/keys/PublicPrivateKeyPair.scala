package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

case class PublicPrivateKeyPair(id: UUID, publicKey: String, privateKey: String)
