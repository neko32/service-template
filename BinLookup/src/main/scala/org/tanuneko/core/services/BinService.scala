package org.tanuneko.core.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{ EntityStreamingSupport, JsonEntityStreamingSupport }
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest }
import org.tanuneko.core.models._
import akka.http.scaladsl.model.headers.RawHeader
import com.typesafe.scalalogging.LazyLogging
import org.tanuneko.ops.BinRetrievalOps

import scala.concurrent.{ ExecutionContext, Future }

trait BinService {
  def lookup(bin: String): Future[Either[ErrorResponse, BinInfo]]
}

class DefaultBinService(binOps: BinRetrievalOps)(implicit
    ac: ActorSystem,
    ec: ExecutionContext
) extends BinService
    with LazyLogging {

  override def lookup(bin: String): Future[Either[ErrorResponse, BinInfo]] = binOps.retrieveBIN(bin)

}
