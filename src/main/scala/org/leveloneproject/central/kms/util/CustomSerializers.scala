package org.leveloneproject.central.kms.util

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.{TemporalAccessor, TemporalQuery}

import org.json4s.{CustomSerializer, MappingException}
import org.json4s.JsonAST.JString
import org.leveloneproject.central.kms.domain.healthchecks._

object CustomSerializers {

  val defaults = Seq(InstantSerializer, HealthCheckLevelSerializer, HealthCheckStatusSerializer)

  object InstantSerializer extends InstantSerializer(DateTimeFormatter.ISO_INSTANT)

  object HealthCheckLevelSerializer extends HealthCheckLevelSerializer

  object HealthCheckStatusSerializer extends HealthCheckStatusSerializer

  class InstantSerializer(val format: DateTimeFormatter) extends CustomSerializer[Instant](_ ⇒ (
    {
      case JString(s) => format.parse(s, asQuery(Instant.from))
    },
    {
      case t: Instant ⇒ JString(format.format(t))
    }
    ))

  class HealthCheckLevelSerializer extends CustomSerializer[HealthCheckLevel](_ ⇒ (
    {
      case JString(s) ⇒ s match {
        case "ping" ⇒ HealthCheckLevel.Ping
        case _ ⇒ throw new MappingException("")
      }
    },
    {
      case l: HealthCheckLevel ⇒ JString(l.value)
    }
  ))

  class HealthCheckStatusSerializer extends CustomSerializer[HealthCheckStatus](_ ⇒ (
    {
      case JString(s) ⇒ HealthCheckStatus(s)
    },
    {
      case l: HealthCheckStatus ⇒ JString(l.value)
    }
  ))

  def asQuery[T](f: TemporalAccessor ⇒ T): TemporalQuery[T] =
    (temporal: TemporalAccessor) => f(temporal)
}