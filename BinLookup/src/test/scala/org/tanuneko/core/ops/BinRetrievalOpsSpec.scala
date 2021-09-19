package org.tanuneko.core.ops

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ AttributeKey, HttpEntity, HttpHeader, HttpProtocols, HttpResponse }
import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.wordspec.AsyncWordSpecLike
import org.tanuneko.core.TestData
import org.tanuneko.ops.{ BinHttpOps, DefaultBinRetrievalOps }

import scala.concurrent.Future

class BinRetrievalOpsSpec
    extends TestKit(ActorSystem("BinRetrievalOpsSpec"))
    with Matchers
    with MockFactory
    with AsyncWordSpecLike {

  implicit val ec             = system.dispatcher
  implicit val ac             = system
  implicit val binHttpOpsMock = mock[BinHttpOps]

  "BinRetrievalOps returns bin data on successful communication with external bin service" in {

    implicit val binHttpOpsMock = stub[BinHttpOps]

    val mockHttpResp = TestData.generateTestHttpResponse(200, TestData.testBinData)

    (binHttpOpsMock.sendHttpReq _).when(*, *).returns(Future.successful(mockHttpResp))

    val binOps = new DefaultBinRetrievalOps(binHttpOpsMock, "http://test.com")

    binOps
      .retrieveBIN("123456")
      .map { result =>
        result match {
          case Right(bin) =>
            bin.number.length must equal(16)
            bin.scheme must equal("visa")
            bin.country.currency must equal("DKK")
            bin.bank.name must equal("Jyske Bank")
          case Left(_) => fail("shouldn't be left")
        }
      }
  }

  "BinRetrievalOps ends up with Left with 400 result from external bin service" in {

    implicit val binHttpOpsMock = stub[BinHttpOps]

    val mockHttpResp = new HttpResponse(
      status = 400,
      // binlist.net returns empty for non-200 cases
      entity = HttpEntity(""),
      headers = scala.collection.immutable.Seq.empty[HttpHeader],
      attributes = Map.empty[AttributeKey[_], Any],
      protocol = HttpProtocols.`HTTP/2.0`
    )

    (binHttpOpsMock.sendHttpReq _).when(*, *).returns(Future.successful(mockHttpResp))

    val binOps = new DefaultBinRetrievalOps(binHttpOpsMock, "http://test.com")

    binOps
      .retrieveBIN("123456")
      .map { result =>
        result match {
          case Right(_) => fail("should be left")
          case Left(e) =>
            e.id must equal("BINSVC_ERR_400")
            e.descr must equal("err - 400")
        }
      }
  }

  "BinRetrievalOps ends up with Left with 500 result from external bin service" in {

    implicit val binHttpOpsMock = stub[BinHttpOps]

    val mockHttpResp = new HttpResponse(
      status = 500,
      // binlist.net returns empty for non-200 cases
      entity = HttpEntity(""),
      headers = scala.collection.immutable.Seq.empty[HttpHeader],
      attributes = Map.empty[AttributeKey[_], Any],
      protocol = HttpProtocols.`HTTP/2.0`
    )

    (binHttpOpsMock.sendHttpReq _).when(*, *).returns(Future.successful(mockHttpResp))

    val binOps = new DefaultBinRetrievalOps(binHttpOpsMock, "http://test.com")

    binOps
      .retrieveBIN("123456")
      .map { result =>
        result match {
          case Right(_) => fail("should be left")
          case Left(e) =>
            e.id must equal("BINSVC_ERR_500")
            e.descr must equal("err - 500")
        }
      }
  }

}
