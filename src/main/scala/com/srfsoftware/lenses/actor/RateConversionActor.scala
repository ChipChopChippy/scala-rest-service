package com.srfsoftware.lenses.actor

import akka.actor.{Actor, ActorLogging}
import akka.http.caching.scaladsl.Cache
import com.srfsoftware.lenses.conversion.ExchangeRateConverter
import com.srfsoftware.lenses.domain.{Conversion, ExchangeRate}

import scala.util.{Failure, Success, Try}

object RateConversionActor {
  trait Action
  final case class ErrorAction(error: String) extends Action
  final case class ActionPerformed(exchange: Double, amount: Double, original: Double) extends Action
  final case class CreateConversion(conv: Conversion)
}

class RateConversionActor(cache: Cache[String, ExchangeRate]) extends Actor with ActorLogging {
  import RateConversionActor._
  implicit val lfuCache = cache

  def receive: Receive = {
    case CreateConversion(conv) =>
      val cv = Try{ExchangeRateConverter.convert(conv.fromCurrency, conv.toCurrency, conv.amount)}
      cv match {
        case Success(value) => sender() ! ActionPerformed(value.exchange,value.amount,value.original)
        case Failure(exception) => sender() ! ErrorAction(exception.getMessage)
      }
    }
}