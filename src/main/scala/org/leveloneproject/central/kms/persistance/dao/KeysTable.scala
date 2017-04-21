package org.leveloneproject.central.kms.persistance.dao

import java.util.UUID

import org.leveloneproject.central.kms.domain.keys.KeyDomain.Key
import slick.jdbc.PostgresProfile.api._

trait KeysTable {

  var keys = TableQuery[KeysDao]

  class KeysDao(tag: Tag) extends Table[Key](tag, "Keys") {
    def id = column[UUID]("id")
    def publicKey = column[String]("public_key")
    def * = (id, publicKey) <> (Key.tupled, Key.unapply)
  }
}