package org.leveloneproject.central.kms.persistance.dao

import java.util.UUID

import org.leveloneproject.central.kms.domain.keys.Key
import slick.jdbc.PostgresProfile.api._

trait KeysTable {
  var keys = TableQuery[KeysDao]

  class KeysDao(tag: Tag) extends Table[Key](tag, "Keys") {
    def id = column[UUID]("id")
    def serviceName = column[String]("service_name")
    def publicKey = column[String]("public_key")
    def * = (id, serviceName, publicKey) <> (Key.tupled, Key.unapply)
  }
}
