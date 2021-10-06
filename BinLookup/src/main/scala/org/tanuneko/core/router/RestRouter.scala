package org.tanuneko.core.router

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCode, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging
import org.tanuneko.core.services._
import org.tanuneko.util.JsonResponseHandling
import spray.json._
import org.tanuneko.core.models._

import scala.util.{ Failure, Success }

class RestRouter(healthcheckService: HealthCheckService, binService: BinService)
    extends JsonResponseHandling
    with LazyLogging {

  import ErrorResponseJsonProtocol._

  def route: Route = healthRoute ~ binRoute

  def healthRoute =
    pathPrefix("health") {
      pathEnd {
        val _ = healthcheckService.shallowCheck
        asJsonResponse(StatusCode.int2StatusCode(200), "healthy@shallowcheck")
      } ~
        path("deep") {
          logger.info("received deep health check request..")
          healthcheckService.deepCheck match {
            case HealthyStatus => asJsonResponse(StatusCode.int2StatusCode(200), "healthy@deepcheck")
            case UnhealthyStatus(id, descr) =>
              val errorResp = ErrorResponse(id, descr)
              asJsonResponse(StatusCode.int2StatusCode(500), errorResp.toJson.compactPrint)
          }
        }
    }

  // sample - http://localhost:10520/bin/45717360
  def binRoute = {
    import BinInfoJsonProtocol._
    import ErrorResponseJsonProtocol._
    // [TODO] add validation for segment val
    get {
      path("bin" / Segment) { binNum: String =>
        onComplete(binService.lookup(binNum)) {
          case Success(rez) =>
            rez match {
              case Right(binInfo) =>
                complete(
                  HttpResponse(200, entity = HttpEntity(ContentTypes.`application/json`, binInfo.toJson.prettyPrint))
                )
              case Left(e) =>
                complete(HttpResponse(500, entity = HttpEntity(ContentTypes.`application/json`, e.toJson.compactPrint)))
            }
          case Failure(e:BinFormatException) =>
            complete(HttpResponse(400, entity = HttpEntity(ContentTypes.`application/json`, e.getMessage)))
          case Failure(e) =>
            e.printStackTrace()
            complete(HttpResponse(500, entity = HttpEntity(ContentTypes.`application/json`, e.getMessage)))
        }
      }
    }
  }
}
