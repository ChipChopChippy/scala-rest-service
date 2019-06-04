package com.srfsoftware.lenses.cache

import akka.NotUsed
import akka.actor.Cancellable
import akka.http.caching.scaladsl.Cache
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.srfsoftware.lenses.LensesHttpServer.system
import com.srfsoftware.lenses.domain.ExchangeRate
import com.srfsoftware.lenses.json.ParseJson
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits._

object CacheService {
  val conf = ConfigFactory.load()
  val request = RequestBuilding.Get(Uri(conf.getString("exchangeRateUri")))
  val source: Source[HttpRequest, Cancellable] = Source.tick(1.seconds, 60.seconds, request)

  def start(implicit materializer: ActorMaterializer, cache: Cache[String, ExchangeRate])= sourceWithDest.runForeach {
    resp =>
      resp.get.entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        val er = ParseJson.parse(body.utf8String)
        cache.remove(conf.getString("cacheKey"))
        cache.getOrLoad(conf.getString("cacheKey"), _ => Future.successful(er))
        println(s"${ParseJson.parse(body.utf8String)}")
      }
  }

  def sourceWithDest: Source[Try[HttpResponse], Cancellable] = source.map(req ⇒ (req, NotUsed)).via(Http().superPool[NotUsed]()).map(_._1)
}