package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

trait CreateError {
  val message: String
}

object CreateError {
  case class KeyExists(id: UUID) extends CreateError {
    val message: String = "Key with id '%s' already exists".format(id)
  }

  case class DatabaseFailed() extends CreateError {
    val message: String = "Error communicating with underlying data store"
  }

  case class CreateFailed() extends CreateError {
    val message: String = "Error creating key"
  }
}
