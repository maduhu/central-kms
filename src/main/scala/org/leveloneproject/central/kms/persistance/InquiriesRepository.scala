package org.leveloneproject.central.kms.persistance

import org.leveloneproject.central.kms.domain.inquiries.Inquiry

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait InquiriesRepository extends InquiriesTable {
  this: DbProfile ⇒

  import profile.api._

  val dbProvider: DbProvider

  private val db = dbProvider.db

  def insert(inquiry: Inquiry): Future[Inquiry] = db.run { inquiries += inquiry } map { _ ⇒ inquiry}
}
