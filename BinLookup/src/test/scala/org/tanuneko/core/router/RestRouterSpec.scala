package org.tanuneko.core.router

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestKit
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.tanuneko.core.TestData
import org.tanuneko.core.models.{ BinInfo, ErrorResponse }
import org.tanuneko.core.services.{ DefaultBinService, DefaultHealthCheckService }
import org.tanuneko.ops.BinRetrievalOps

import scala.concurrent.Future
import spray.json._

class RestRouterSpec extends AnyWordSpec with Matchers with MockFactory with ScalatestRouteTest {

  implicit val ec = system.dispatcher
  implicit val ac = system

  val healthCheckService = new DefaultHealthCheckService

  "Shallow Health Check returns Healthy Status" in {
    val binRetrievalOpsMock = stub[BinRetrievalOps]

    (binRetrievalOpsMock.retrieveBIN _).when(*).returns(Future.successful(Right(TestData.testBinDataAsBinInfo)))
    val binService = new DefaultBinService(binRetrievalOpsMock)

    val restRouter = new RestRouter(healthCheckService, binService)
    Get("/health") ~> restRouter.route ~> check {
      status.intValue() must equal(200)
      entityAs[String] must equal("healthy@shallowcheck")
    }

  }

  "Deep Health Check returns either Healthy or Unhealthy Status" in {
    val binRetrievalOpsMock = stub[BinRetrievalOps]

    (binRetrievalOpsMock.retrieveBIN _).when(*).returns(Future.successful(Right(TestData.testBinDataAsBinInfo)))
    val binService = new DefaultBinService(binRetrievalOpsMock)

    val restRouter = new RestRouter(healthCheckService, binService)
    Get("/health/deep") ~> restRouter.route ~> check {
      status.intValue() must equal(500)
      entityAs[String] must equal("{\"descr\":\"NOSQL DB NOT RESPONDING\",\"id\":\"ERR-1\"}")
    }
    Get("/health/deep") ~> restRouter.route ~> check {
      status.intValue() must equal(200)
      entityAs[String] must equal("healthy@deepcheck")
    }

  }

  "Bin Service lookup ends up with successful result" in {
    import org.tanuneko.core.models.BinInfoJsonProtocol._
    val binRetrievalOpsMock = stub[BinRetrievalOps]

    (binRetrievalOpsMock.retrieveBIN _).when(*).returns(Future.successful(Right(TestData.testBinDataAsBinInfo)))
    val binService = new DefaultBinService(binRetrievalOpsMock)

    val restRouter = new RestRouter(healthCheckService, binService)
    Get("/bin/123456") ~> restRouter.route ~> check {
      status.intValue() must equal(200)
      val result  = entityAs[String]
      val binInfo = result.asJson.convertTo[BinInfo]
      binInfo.bank.name must equal("Jyske Bank")
      binInfo.brand must equal("Visa/Dankort")
      binInfo.country.alpha2 must equal("DK")
      binInfo.number.length must equal(16)
    }

  }

  "Bin Service lookup ends up fails with some error status due to external service's non-200 result" in {
    val binRetrievalOpsMock = stub[BinRetrievalOps]

    (binRetrievalOpsMock.retrieveBIN _)
      .when(*)
      .returns(Future.successful(Left(ErrorResponse(id = "ERRID", descr = "ERRDESCR"))))
    val binService = new DefaultBinService(binRetrievalOpsMock)

    val restRouter = new RestRouter(healthCheckService, binService)
    Get("/bin/123456") ~> restRouter.route ~> check {
      status.intValue() must equal(500)
      entityAs[String] must equal("{\"descr\":\"ERRDESCR\",\"id\":\"ERRID\"}")
    }

  }

  "Bin Service lookup ends up Future Failure" in {
    val binRetrievalOpsMock = stub[BinRetrievalOps]

    (binRetrievalOpsMock.retrieveBIN _).when(*).returns(Future.failed(new Exception("ERR")))
    val binService = new DefaultBinService(binRetrievalOpsMock)

    val restRouter = new RestRouter(healthCheckService, binService)
    Get("/bin/123456") ~> restRouter.route ~> check {
      status.intValue() must equal(500)
      entityAs[String] must equal("ERR")
    }

  }

}
