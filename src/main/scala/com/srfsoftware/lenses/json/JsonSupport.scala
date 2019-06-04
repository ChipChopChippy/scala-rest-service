package com.srfsoftware.lenses.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.srfsoftware.lenses.actor.RateConversionActor.{ActionPerformed, ErrorAction}
import com.srfsoftware.lenses.domain.{Conversion, ExchangeRate, Rates}
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._
  implicit val rateJsonFormat = jsonFormat2(Rates)
  implicit val ratesJsonFormat = jsonFormat3(ExchangeRate)
  implicit val conversionJsonFormat = jsonFormat3(Conversion)
  implicit val actionPerformedJsonFormat = jsonFormat3(ActionPerformed)
  implicit val errorActionJsonFormat = jsonFormat1(ErrorAction)
}