package com.srfsoftware.lenses.routes

import akka.actor.{ActorRef, Props}
import akka.http.caching.LfuCache
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.srfsoftware.lenses.actor.RateConversionActor
import com.srfsoftware.lenses.domain.{Conversion, ExchangeRate, Rates}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FunSuite, Matchers}

import scala.concurrent.Future

class ExchangeRateRoutesTest extends FunSuite with Matchers with ScalaFutures with ScalatestRouteTest
    with ExchangeRateRoutes {

  val cache = LfuCache[String, ExchangeRate]
  override val rateConversionActor: ActorRef = system.actorOf(Props(classOf[RateConversionActor], cache), "exchangeRateRegistryActor")
  lazy val erRoutes = routes


  test("erRoutes add  (api /  convert)") {
    val er = ExchangeRate("EUR", List(Rates("USD", 1.1134), Rates("GBP", 0.88178)), "2019-05-31")
    val conv = Conversion(fromCurrency = "USD", toCurrency = "GBP", amount = 102.6)
    cache.getOrLoad("KEY", _ => Future.successful(er))
    val convEntity = Marshal(conv).to[MessageEntity].futureValue
    val request = Post("/api/convert").withEntity(convEntity)

    request ~> erRoutes ~> check {
      status should ===(StatusCodes.Created)
      contentType should ===(ContentTypes.`application/json`)
      entityAs[String] should ===("""{"amount":81.25617747440273,"exchange":0.7919705406861866,"original":102.6}""")
    }
  }

  test("an error should be returned when the URI is incorrect") {
    val er = ExchangeRate("EUR", List(Rates("USD", 1.1134), Rates("GBP", 0.88178)), "2019-05-31")
    val conv = Conversion(fromCurrency = "USD", toCurrency = "GBP", amount = 102.6)
    cache.getOrLoad("KEY", _ => Future.successful(er))
    val convEntity = Marshal(conv).to[MessageEntity].futureValue
    val request = Post("/api/converter").withEntity(convEntity)
    request ~> erRoutes ~> check {
      status should ===(StatusCodes.NotFound)
    }
  }
}