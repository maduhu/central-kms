package org.leveloneproject.central.kms.util

import scala.language.implicitConversions

object Bytes {
  def fromHex(hex: String): Array[Byte] = {
    hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

  implicit class Hex(buf: Array[Byte]) {
    def toHex = buf.map("%02X" format _).mkString
  }
}
