package org.tanuneko.ops

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{ EntityStreamingSupport, JsonEntityStreamingSupport }
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest, HttpResponse }
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import org.tanuneko.core.models.{ BinInfo, BinInfoJsonProtocol }
import spray.json._

import scala.concurrent.{ ExecutionContext, Future }

trait BinHttpOps {
  def sendHttpReq(bin: String, url: String): Future[HttpResponse]
}

class DefaultBinHttpOps(implicit ac: ActorSystem, ec: ExecutionContext) extends BinHttpOps {
  override def sendHttpReq(bin: String, url: String): Future[HttpResponse] =
    Http()
      .singleRequest(
        HttpRequest(method = HttpMethods.GET, uri = s"${url}/${bin}")
          .withHeaders(RawHeader("Accept-Version", "3"))
      )
}

trait BinRetrievalOps {
  def retrieveBIN(bin: String): Future[Either[Exception, BinInfo]]
}

class DefaultBinRetrievalOps(BinHttpOps: BinHttpOps, binSvcUrl: String)(implicit
    ac: ActorSystem,
    ec: ExecutionContext
) extends BinRetrievalOps
    with LazyLogging {

  override def retrieveBIN(bin: String): Future[Either[Exception, BinInfo]] = {
    implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
      EntityStreamingSupport.json()

    import BinInfoJsonProtocol._
    logger.info(s"bin - ${bin}")
    BinHttpOps
      .sendHttpReq(bin, binSvcUrl)
      .flatMap { x =>
        x.status.intValue match {
          case rez if rez == 200 =>
            val retVal = x.entity.dataBytes
              .runWith(Sink.fold(ByteString.empty)(_ ++ _))
              .map(_.utf8String) map { rez =>
              rez.asJson.convertTo[BinInfo]
            }
            retVal.map(Right(_))
          case e => Future(Left(new Exception(s"err - ${x.status.intValue}")))
        }
      }
  }

}
