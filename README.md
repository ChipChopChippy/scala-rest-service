# Sample scala rest service exercise using akka-http
Create a REST application with a single endpoint

##
POST /api/convert
Body:
{
"fromCurrency": "GBP",
"toCurrency" : "EUR",
"amount" : 102.6
}

The return should be an object with the exchange rate between the "fromCurrency" to "toCurrency"
and the amount converted to the second currency.
{
"exchange" : 1.11,
"amount" : 113.886,
"original" : 102.6
}

The exchange rates should be loaded from https://exchangeratesapi.io and assume the currency
rates change every 1 minute.

## An example test using curl
curl -H "Content-type: application/json" -X POST -d '{"fromCurrency": "BGN", "toCurrency": "EUR", "amount":102.6}' http://localhost:8080/api/convert
