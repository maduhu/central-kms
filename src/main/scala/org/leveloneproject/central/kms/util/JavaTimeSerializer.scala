package org.leveloneproject.central.kms.util

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.{TemporalAccessor, TemporalQuery}

import org.json4s.CustomSerializer
import org.json4s.JsonAST.JString

object JavaTimeSerializer {

  val defaults = Seq(InstantSerializer)

  object InstantSerializer extends InstantSerializer(DateTimeFormatter.ISO_INSTANT)

  class InstantSerializer(val format: DateTimeFormatter) extends CustomSerializer[Instant](a ⇒ (
    {
      case JString(s) => format.parse(s, asQuery(Instant.from))
    },
    {
      case t: Instant ⇒ JString(format.format(t))
    }
    ))

  def asQuery[T](f: TemporalAccessor ⇒ T): TemporalQuery[T] =
    (temporal: TemporalAccessor) => f(temporal)
}