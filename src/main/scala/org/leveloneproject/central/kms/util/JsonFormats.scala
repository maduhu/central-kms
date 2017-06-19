package org.leveloneproject.central.kms.util

import org.json4s.{DefaultFormats, Formats}
import org.json4s.ext.JavaTypesSerializers

trait JsonFormats {
  implicit val formats: Formats = DefaultFormats ++ JavaTypesSerializers.all ++ CustomSerializers.defaults
}
