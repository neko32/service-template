package org.tanuneko.core.services

import scala.concurrent.ExecutionContext
import scala.util.Random

sealed trait HealthStatus

case object HealthyStatus extends HealthStatus

case class UnhealthyStatus(id: String, descr: String) extends HealthStatus

trait HealthCheckService {

  def shallowCheck: HealthStatus
  def deepCheck: HealthStatus

}

class DefaultHealthCheckService(implicit
    ec: ExecutionContext
) extends HealthCheckService {

  var cnt = 0

  // always return healthy if this service is reachable
  override def shallowCheck: HealthStatus = HealthyStatus

  // implement more deep check to check all dependencies' status
  override def deepCheck: HealthStatus = {
    // very temporal code.
    cnt = (cnt + 1) % 3
    cnt match {
      case n if n == 2 => HealthyStatus
      case n if n == 1 => UnhealthyStatus("ERR-1", "NOSQL DB NOT RESPONDING")
      case n if n == 0 => UnhealthyStatus("ERR-2", "EXTERNAL BIN SERVICE NOT RESPONDING")
    }
  }

}
