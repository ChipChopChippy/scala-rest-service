package com.srfsoftware.lenses

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.caching.LfuCache
import com.srfsoftware.lenses.cache.CacheService
import com.srfsoftware.lenses.domain.ExchangeRate
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.srfsoftware.lenses.actor.RateConversionActor
import com.srfsoftware.lenses.routes.ExchangeRateRoutes

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object LensesHttpServer extends App with ExchangeRateRoutes {

  implicit val system: ActorSystem = ActorSystem("lensesHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher
  implicit val cache = LfuCache[String, ExchangeRate]
  val rateConversionActor: ActorRef = system.actorOf(Props(classOf[RateConversionActor], cache), "exchangeRateRegistryActor")

  lazy val erRoutes: Route = routes

  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(erRoutes, "localhost", 8080)

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }
  CacheService.start
  Await.result(system.whenTerminated, Duration.Inf)
}