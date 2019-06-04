package com.srfsoftware.lenses.domain

final case class ExchangeRate(base: String, rates: List[Rates], date: String)
final case class Rates(code: String, rate: Double)
final case class Currency(exchange: Double, amount: Double, original: Double)
final case class Conversion(fromCurrency: String, toCurrency: String, amount: Double)
