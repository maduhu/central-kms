package org.leveloneproject.central.kms.routing

import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.leveloneproject.central.kms.util.JsonFormats
import org.json4s.{Formats, native}

trait JsonSupport extends Json4sSupport with JsonFormats {

  implicit val serialization = native.Serialization

  implicit def jsonFormats: Formats = formats
}
