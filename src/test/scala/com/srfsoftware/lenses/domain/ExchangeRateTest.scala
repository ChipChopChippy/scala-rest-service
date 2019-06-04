package com.srfsoftware.lenses.domain

import akka.actor.ActorSystem
import akka.http.caching.LfuCache
import com.srfsoftware.lenses.conversion.ExchangeRateConverter
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.Future

class ExchangeRateTest extends FunSuite with Matchers {

  implicit val system: ActorSystem = ActorSystem()
  implicit val cache = LfuCache[String, ExchangeRate]

  val er = ExchangeRate("EUR", List(Rates( "USD", 1.1134), Rates("GBP", 0.88178)), "2019-05-31")
  cache.getOrLoad("KEY", _ => Future.successful(er))

  test("should return a currency object") {
    assert(ExchangeRateConverter.convert("GBP", "EUR", 102.6) == Currency(1.134069722606546, 116.35555353943161, 102.6))
  }

  test("USD into GBP should be correct") {
    assert(ExchangeRateConverter.convert("USD", "GBP", 102.6) == Currency(0.7919705406861866, 81.25617747440273, 102.6))
  }

  test("should return an error with unknown currency when unlisted from currency is submitted") {
    val thrown = intercept[NoSuchElementException] {
      ExchangeRateConverter.convert("XXX", "GBP", 102.6)
    }
    assert(thrown.getMessage == "XXX is an invalid from currency")
  }

  test("should return an error with unknown currency when unlisted to currency is submitted") {
    val thrown = intercept[NoSuchElementException] {
      ExchangeRateConverter.convert("GBP", "XXX", 102.6)
    }
    assert(thrown.getMessage == "XXX is an invalid to currency")
  }
}