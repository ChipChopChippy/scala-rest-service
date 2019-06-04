package com.srfsoftware.lenses.conversion

import akka.http.caching.scaladsl.Cache
import com.srfsoftware.lenses.domain.{Currency, ExchangeRate}
import com.typesafe.config.ConfigFactory

import scala.util.Try

object ExchangeRateConverter {
  val conf = ConfigFactory.load()

  def convert(fromCurrency: String, toCurrency: String, amount: Double)(implicit cache: Cache[String, ExchangeRate]): Currency = {
    val ch = Try {cache.get(conf.getString("cacheKey")).flatMap { v => v.value.map(w => w.get)}}

    def g(curr: String, fromCurr: Boolean): (String, Double) = {
      ch.get match {
        case e if e.get.base == curr => (e.get.base, 1.0)
        case e => e.get.rates.find(_.code == curr).map(c => (c.code, c.rate))
          .getOrElse(throw new NoSuchElementException(s"$curr is an invalid ${toOrFrom(fromCurr)}"))
      }
    }

    ch.get match {
      case Some(er) =>
        val from = g(fromCurrency, true)
        val to = g(toCurrency, false)
        val rateFactor = if (fromCurrency != er.base) 1.0 / from._2 else 1.0
        Currency(rateFactor * to._2, rateFactor * to._2 * amount, amount)
      case _ =>
        throw new Error(s"there has been an error retrieving the exchange rates. " +
          s"Check that the server ${conf.getString("exchangeRateUri")} is available.")
    }
  }
  private def toOrFrom(bool: Boolean): String = {
    if (bool) "from currency" else "to currency"
  }
}
