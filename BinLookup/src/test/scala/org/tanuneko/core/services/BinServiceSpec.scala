package org.tanuneko.core.services

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import org.tanuneko.core.TestData
import org.tanuneko.ops.BinRetrievalOps

import scala.concurrent.Future

class BinServiceSpec
    extends TestKit(ActorSystem("BinServiceSpec"))
    with Matchers
    with MockFactory
    with AsyncWordSpecLike {

  implicit val ec = system.dispatcher
  implicit val ac = system

  "BiNService executes Bin Retrieval operation and receives Right on successful result" in {

    val binRetrievalOpsMock = stub[BinRetrievalOps]

    (binRetrievalOpsMock.retrieveBIN _).when(*).returns(Future.successful(Right(TestData.testBinDataAsBinInfo)))
    val binService:BinService = new DefaultBinService(binRetrievalOpsMock)
    binService.lookup("111111") map {
      case Left(_) => fail("should be right")
      case Right(bin) =>
        bin.country.name must equal("Denmark")
        bin.`type` must equal("debit")
        bin.number.length must equal(16)
        bin.bank.name must equal("Jyske Bank")
    }

  }

  "BiNService executes Bin Retrieval operation and receives Left on failed result" in {

    val binRetrievalOpsMock = stub[BinRetrievalOps]

    (binRetrievalOpsMock.retrieveBIN _).when(*).returns(Future.successful(Left(new Exception("ERR"))))
    val binService:BinService = new DefaultBinService(binRetrievalOpsMock)
    binService.lookup("111111") map {
      case Right(_) => fail("should be left ")
      case Left(ex) =>
        ex.getMessage must equal("ERR")
    }

  }

}
