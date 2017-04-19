package org.leveloneproject.central.kms.util

object Bytes {
  def fromHex(hex: String): Array[Byte] = {
    hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

  def toHex(buf: Array[Byte]): String = buf.map("%02X" format _).mkString
}
