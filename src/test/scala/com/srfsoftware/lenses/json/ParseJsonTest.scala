package com.srfsoftware.lenses.json

import org.scalatest.{FunSuite, Matchers}

class ParseJsonTest extends FunSuite with Matchers {

  test("json document should have a base rate of EUR") {
    assert(ParseJson.parseFromFile("/erSample.json").base === "EUR")
  }
  test("json document should have a list of exchange rates of length 32") {
    assert(ParseJson.parseFromFile("/erSample.json").rates.length === 32)
  }
  test("JPY should have an exchange rate of 122.1") {
    assert(ParseJson.parseFromFile("/erSample.json").rates.find(c => c.code == "JPY").map(r => r.rate).getOrElse(-1) === 122.1)
  }

}
