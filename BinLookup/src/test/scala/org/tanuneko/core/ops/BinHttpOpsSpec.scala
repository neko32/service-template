package org.tanuneko.core.ops

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import org.tanuneko.ops.DefaultBinHttpOps
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.scalatest.{ BeforeAndAfter, BeforeAndAfterAll }
import org.tanuneko.core.TestData

class BinHttpOpsSpec
    extends TestKit(ActorSystem("BinHttpOpsSpec"))
    with Matchers
    with AsyncWordSpecLike
    with BeforeAndAfter {

  implicit val ec                            = system.dispatcher
  implicit val ac                            = system
  val testSite                               = "http://localhost:32510"
  val binNum                                 = "123456"
  var wireMockServer: Option[WireMockServer] = None

  before {
    if (!wireMockServer.isDefined) {
      wireMockServer = Some(new WireMockServer(WireMockConfiguration.wireMockConfig().port(32510)))
      wireMockServer.get.start()
      WireMock.configureFor("localhost", wireMockServer.get.port())
    }
  }

  after {
    if (wireMockServer.isDefined) {
      wireMockServer.get.resetAll()
      wireMockServer.get.stop()
      wireMockServer = None
    }
  }

  "BinHttpOps sends http request" in {
    val httpOps = new DefaultBinHttpOps()
    stubFor(get(s"/${binNum}").willReturn(ok(TestData.testBinData)))

    httpOps
      .sendHttpReq(bin = binNum, url = testSite)
      .map { resp =>
        resp.status.intValue() must equal(200)
      }

  }

}
