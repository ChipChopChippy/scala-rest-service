package com.srfsoftware.lenses.cache

import akka.actor.ActorSystem
import akka.http.caching.LfuCache
import org.scalatest.{FunSuite, Matchers}
import com.srfsoftware.lenses.domain.{ExchangeRate, Rates}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

class CacheServiceTest extends FunSuite with Matchers {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  test("an empty cache should have a size of 0") {
    val cache = LfuCache[String, String]
    cache.keys should be (Set())
    cache.size should be(0)
  }

  test("value should be stored in the cache") {
    val cache = LfuCache[String, String]
    Await.result(cache.get("1", () => "A"), 3.seconds) should be("A")
  }

  test("should store an exchange rate object") {
    val cache = LfuCache[String, ExchangeRate]
    val er = ExchangeRate("EUR", List(Rates("GBP", 1.25)), "2019-05-31")
    Await.result(cache.getOrLoad("KEY", _ => Future.successful(er)), 3.seconds).base should be("EUR")
  }

  test("should update the cache with a new exchange rate") {
    val cache = LfuCache[String, ExchangeRate]
    val er1 = ExchangeRate("EUR", List(Rates("GBP", 1.25)), "2019-05-31")
    val er2 = ExchangeRate("EUR", List(Rates("GBP", 1.29)), "2019-06-01")
    Await.result(cache.getOrLoad("KEY", _ => Future.successful(er1)), 3.seconds).rates.head.rate should be(1.25)
    cache.remove("KEY")
    Await.result(cache.getOrLoad("KEY", _ => Future.successful(er2)), 3.seconds).rates.head.rate should be(1.29)
  }
}
