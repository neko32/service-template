package org.tanuneko.core.services

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class HealthChecKServiceSpec
    extends TestKit(ActorSystem("HealthCheckServiceSpec"))
    with Matchers
    with MockFactory
    with AnyWordSpecLike {

  implicit val ec = system.dispatcher

  "HealthCheckService shallow health check always return Healthy" in {

    val healthCheckService = new DefaultHealthCheckService
    healthCheckService.shallowCheck must equal(HealthyStatus)

  }

  "HealthCheckService deep health check returns either healthy or unhealthy status" in {

    val healthCheckService = new DefaultHealthCheckService
    val first              = healthCheckService.deepCheck
    first match {
      case UnhealthyStatus(id, descr) =>
        id must equal("ERR-1")
        descr must equal("NOSQL DB NOT RESPONDING")
      case _ => fail("should be Unhealthy with ERR-1")
    }
    val second = healthCheckService.deepCheck
    second match {
      case UnhealthyStatus(_, _) => fail("must be HealthyStatus")
      case _                     => // good
    }
    val third = healthCheckService.deepCheck
    third match {
      case UnhealthyStatus(id, descr) =>
        id must equal("ERR-2")
        descr must equal("EXTERNAL BIN SERVICE NOT RESPONDING")
      case _ => fail("must be ERR-2")
    }
  }

}
