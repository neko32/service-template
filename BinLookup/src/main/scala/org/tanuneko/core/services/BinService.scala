package org.tanuneko.core.services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{ EntityStreamingSupport, JsonEntityStreamingSupport }
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest }
import org.tanuneko.core.models._
import akka.http.scaladsl.model.headers.RawHeader
import com.typesafe.scalalogging.LazyLogging
import org.tanuneko.ops.{ BinRetrievalOps, CacheOps }

import scala.concurrent.{ ExecutionContext, Future }

trait BinService {
  def lookup(bin: String): Future[Either[ErrorResponse, BinInfo]]
  def validateBin(bin: String): Future[Boolean]
}

class DefaultBinService(binOps: BinRetrievalOps)(implicit
    ac: ActorSystem,
    ec: ExecutionContext,
    cacheOps: CacheOps[String]
) extends BinService
    with LazyLogging {

  override def lookup(bin: String): Future[Either[ErrorResponse, BinInfo]] = {
    for {
      validationResult <- validateBin(bin)
      y <- if(validationResult) binOps.retrieveBIN(bin, cacheOps) else Future.failed(new BinFormatException(s"BIN ${bin} format is wrong"))
    } yield y
  }

  override def validateBin(bin:String): Future[Boolean] = Future.successful {
    val allowedSize = Seq(6, 8)
    if (allowedSize.contains(bin.length) && bin.forall(_.isDigit)) true else false
  }

}
