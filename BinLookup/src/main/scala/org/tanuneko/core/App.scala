package org.tanuneko.core

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.tanuneko.core.router.RestRouter
import org.tanuneko.core.services._
import org.tanuneko.ops.{ DefaultBinHttpOps, DefaultBinRetrievalOps }

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{ Failure, Success }

object App {

  // $COVERAGE-OFF$
  def main(args: Array[String]): Unit = {

    implicit val cfg = ConfigFactory.load("main")
    implicit val sys = ActorSystem("tanuapp_sample", cfg)
    implicit val ec  = sys.dispatcher
    implicit val tm  = Timeout(10 seconds)

    // config
    // TODO - add validation
    val (host, port) = (cfg.getString("app.host"), cfg.getInt("app.port"))
    val appName      = cfg.getString("app.akka.systemName")
    val binSvcUrl    = cfg.getString("binService.url")

    // ops
    val httpOps = new DefaultBinHttpOps
    val binOps  = new DefaultBinRetrievalOps(httpOps, binSvcUrl)

    // service
    val healthcheckService = new DefaultHealthCheckService
    val binService         = new DefaultBinService(binOps)

    // router
    val restRouter = new RestRouter(healthcheckService, binService)
    val allRoutes  = restRouter.route

    // bootstrap
    val binder: Future[ServerBinding] = Http().newServerAt(host, port).bind(allRoutes)
    val log                           = Logging(sys.eventStream, appName)
    log.info("starting..")
    binder
      .map { serverBindings =>
        log.info(s"REST API is bound to ${serverBindings.localAddress}")
      }
      .onComplete {
        case Success(_) => log.info(s"Success to bind")
        case Failure(e) =>
          log.error(e, "Failed to bind")
          sys.terminate()
      }
  }
  // $COVERAGE-ON$

}
