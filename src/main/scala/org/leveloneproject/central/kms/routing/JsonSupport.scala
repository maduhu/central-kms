package org.leveloneproject.central.kms.routing

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.leveloneproject.central.kms.util.JsonFormats
import org.json4s.{Formats, native}
import org.json4s.native.Serialization.{write ⇒ jsonWrite}

trait JsonSupport extends Json4sSupport with JsonFormats {

  implicit val serialization = native.Serialization

  implicit def jsonFormats: Formats = formats

  def write[A <: AnyRef](value: A): String = jsonWrite(value)
}
