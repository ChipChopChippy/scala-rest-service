package com.srfsoftware.lenses.json

import java.nio.file.Paths

import com.srfsoftware.lenses.domain.{ExchangeRate, Rates}
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.io.Source

object ParseJson {
  def parseFromFile(str: String): ExchangeRate = {
    val path = Paths.get(getClass.getResource(str).toURI).toString
    val lines = Json.parse(Source.fromFile(path).getLines.mkString)
    ExchangeRate((lines \ "base").asOpt[String].get, parseRate(lines), (lines \ "date").asOpt[String].get)
  }
  def parse(str: String): ExchangeRate = {
    val lines = Json.parse(str)
    ExchangeRate((lines \ "base").asOpt[String].get, parseRate(lines), (lines \ "date").asOpt[String].get)
  }

  private[lenses] def parseRate(jsonValue: JsValue):List[Rates] = {
    val rates = (jsonValue \ "rates").asOpt[JsValue].get
    for {
      rate <- rates.asInstanceOf[JsObject].value.toList
      exRate = Rates(rate._1, rate._2.asOpt[Double].get)
    } yield exRate
  }
}
