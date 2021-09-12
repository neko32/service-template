package org.tanuneko.util

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCode }
import akka.http.scaladsl.server.Directives.complete

trait JsonResponseHandling {

  def asJsonResponse[A <: String](status: StatusCode, content: A) =
    complete(HttpResponse(status, entity = HttpEntity(ContentTypes.`application/json`, content)))

}
