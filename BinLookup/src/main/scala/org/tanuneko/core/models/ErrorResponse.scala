package org.tanuneko.core.models

import spray.json.DefaultJsonProtocol

case class ErrorResponse(id: String, descr: String)

object ErrorResponseJsonProtocol extends DefaultJsonProtocol {
  implicit val errorRespFormat = jsonFormat2(ErrorResponse.apply)
}
