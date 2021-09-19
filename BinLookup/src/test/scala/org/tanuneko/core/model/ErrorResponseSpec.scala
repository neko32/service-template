package org.tanuneko.core.model

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.tanuneko.core.models.ErrorResponse

import spray.json._

class ErrorResponseSpec extends AnyWordSpec with Matchers {

  "ErrorResponse supports Json conv" in {

    import org.tanuneko.core.models.ErrorResponseJsonProtocol._
    val jsonStr = """{
      |  "id":"ID123",
      |  "descr":"DESC"
      |}""".stripMargin
    val emJson  = jsonStr.parseJson
    println(emJson.prettyPrint)
    val emObj = emJson.convertTo[ErrorResponse]
    emObj.id must equal("ID123")
    emObj.descr must equal("DESC")
  }

}
