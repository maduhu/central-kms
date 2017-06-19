package org.leveloneproject.central.kms.domain.sidecars

import java.util.UUID

import akka.actor.ActorRef
import org.leveloneproject.central.kms.domain._

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import scala.concurrent.ExecutionContext.Implicits.global

class SidecarList {

  lazy val sidecars = new mutable.LinkedHashMap[UUID, SidecarWithActor]()

  def current(): Future[Seq[Sidecar]] = Future.successful(sidecars.values.map(_.sidecar).toSeq)

  def register(sidecar: Sidecar, actor: ActorRef): Unit = sidecars += (sidecar.id → SidecarWithActor(sidecar, actor))

  def unregister(id: UUID): Unit = sidecars -= id

  def actorById(id: UUID): Future[Either[Error, ActorRef]] = Future {
    Try(sidecars(id)) match {
      case Success(a) ⇒ Right(a.actor)
      case Failure(_) ⇒ Left(Errors.UnregisteredSidecar(id))
    }
  }

}

case class SidecarWithActor(sidecar: Sidecar, actor: ActorRef)
