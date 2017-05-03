package org.leveloneproject.central.kms

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import org.leveloneproject.central.kms.persistance.Migrator
import org.leveloneproject.central.kms.routing.RouteAggregator
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ServiceSpec extends Specification with Mockito {

  trait Setup extends Scope {
    val migrator = mock[Migrator]
    val routeAggregator = mock[RouteAggregator]
    val service = Service(mock[ActorSystem], mock[ActorMaterializer], migrator, routeAggregator)
  }

  "migrate" should {
    "call migrator migrate" in new Setup {

      service.migrate()

      there was one(migrator).migrate()
    }

    "return routeAggregator route" in new Setup {
      val route = mock[Route]
      routeAggregator.route returns route

      service.route must be(route)
    }
  }
}
