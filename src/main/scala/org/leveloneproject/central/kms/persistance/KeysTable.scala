package org.leveloneproject.central.kms.persistance

import java.util.UUID

import org.leveloneproject.central.kms.domain.Key

trait KeysTable {
  this: DbProfile ⇒

  import profile.api._

  class KeysTable(tag: Tag) extends Table[Key](tag, "Keys") {
    def id = column[UUID]("id")
    def publicKey = column[String]("public_key")
    def * = (id, publicKey) <> (Key.tupled, Key.unapply)
  }

  val keys = TableQuery[KeysTable]
}



