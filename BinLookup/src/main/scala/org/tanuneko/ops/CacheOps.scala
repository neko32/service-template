package org.tanuneko.ops

import redis.RedisClient

import scala.concurrent.Future

trait CacheOps[A] {

  def set(key: String, value: A): Future[Boolean]
  def get(key: String): Future[Option[A]]

}

// TODO integration test - otherwise have to exclude from unit test coverage

class RedisCacheOpsProvider(redisHost: String = "localhost", redisPort: Int = 6379) {

  // $COVERAGE-OFF$

  implicit val stringValueCacheOps = new CacheOps[String] {

    implicit val akkaSystem = akka.actor.ActorSystem()

    val redis = RedisClient()

    override def set(key: String, value: String) =
      redis.set(key, value)

    override def get(key: String) =
      redis.get[String](key)
  }

  // $COVERAGE-ON$

  implicit val inmemStringCacheOps = new CacheOps[String] {

    val inmemCache = scala.collection.mutable.Map.empty[String, String]
    override def set(key: String, value: String): Future[Boolean] = {
      inmemCache(key) = value
      Future.successful(true)
    }

    override def get(key: String): Future[Option[String]] =
      Future.successful(inmemCache.get(key))
  }

}
