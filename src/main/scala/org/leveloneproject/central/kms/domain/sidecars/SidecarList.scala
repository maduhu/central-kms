package org.leveloneproject.central.kms.domain.sidecars

import java.util.UUID

import akka.actor.ActorRef
import org.leveloneproject.central.kms.domain._

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class SidecarList {

  lazy val sidecars = new mutable.LinkedHashMap[UUID, SidecarWithActor]()

  def current(): Future[Seq[Sidecar]] = Future.successful(sidecars.values.map(_.sidecar).toSeq)

  def register(sidecarWithActor: SidecarWithActor): Unit = sidecars += (sidecarWithActor.sidecar.id → sidecarWithActor)

  def unregister(id: UUID): Unit = sidecars -= id

  def actorById(id: UUID): Future[Either[KmsError, ActorRef]] = Future {
    Try(sidecars(id)) match {
      case Success(a) ⇒ Right(a.actor)
      case Failure(_) ⇒ Left(KmsError.unregisteredSidecar(id))
    }
  }

}
