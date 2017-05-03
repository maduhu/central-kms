package org.leveloneproject.central.kms.domain.keys

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.leveloneproject.central.kms.domain.keys.KeyDomain._
import org.specs2.mutable.Specification
import spray.json.RootJsonFormat

class KeyDomainSpec extends Specification with SprayJsonSupport {

  "formats" should {
    "register formats" in {
      KeyDomain.RegistrationResponseFormat must beAnInstanceOf[RootJsonFormat[RegistrationResponse]]
      KeyDomain.RegistrationRequestFormat must beAnInstanceOf[RootJsonFormat[RegistrationRequest]]
      KeyDomain.ValidationRequestFormat must beAnInstanceOf[RootJsonFormat[ValidationRequest]]
      KeyDomain.ValidateErrorFormat must beAnInstanceOf[RootJsonFormat[ValidateError]]
      KeyDomain.ValidateResponseFormat must beAnInstanceOf[RootJsonFormat[ValidateResponse]]
    }
  }
}
