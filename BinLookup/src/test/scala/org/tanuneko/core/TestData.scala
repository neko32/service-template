package org.tanuneko.core

import akka.http.scaladsl.model.{ AttributeKey, HttpEntity, HttpHeader, HttpProtocols, HttpResponse }
import org.tanuneko.core.models.BinInfo

import spray.json._

object TestData {

  lazy val testBinDataAsBinInfo = {
    import org.tanuneko.core.models.BinInfoJsonProtocol._
    testBinData.asJson.convertTo[BinInfo]
  }

  val testBinData =
    """{
      |  "number": {
      |    "length": 16,
      |    "luhn": true
      |  },
      |  "scheme": "visa",
      |  "type": "debit",
      |  "brand": "Visa/Dankort",
      |  "prepaid": false,
      |  "country": {
      |    "numeric": "208",
      |    "alpha2": "DK",
      |    "name": "Denmark",
      |    "emoji": "🇩🇰",
      |    "currency": "DKK",
      |    "latitude": 56,
      |    "longitude": 10
      |  },
      |  "bank": {
      |    "name": "Jyske Bank",
      |    "url": "www.jyskebank.dk",
      |    "phone": "+4589893300",
      |    "city": "Hjørring"
      |  }
      |}
      |""".stripMargin

  def generateTestHttpResponse(status: Int, testData: String) =
    new HttpResponse(
      status = status,
      entity = HttpEntity(testData),
      headers = scala.collection.immutable.Seq.empty[HttpHeader],
      attributes = Map.empty[AttributeKey[_], Any],
      protocol = HttpProtocols.`HTTP/2.0`
    )

}
