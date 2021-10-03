package org.tanuneko.core.ops

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import org.tanuneko.ops.RedisCacheOpsProvider

class CacheOpsSpec extends TestKit(ActorSystem("CacheOpsSpec")) with Matchers with AsyncWordSpecLike {

  "CacheOps simulates string put and get" in {

    val provider = new RedisCacheOpsProvider()

    for {
      _   <- provider.inmemStringCacheOps.set("key", "value")
      res <- provider.inmemStringCacheOps.get("key")
    } yield res match {
      case None    => fail("must be Some")
      case Some(v) => v must equal("value")
    }

  }

}
