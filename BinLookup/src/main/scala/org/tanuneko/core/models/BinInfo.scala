package org.tanuneko.core.models

import spray.json.DefaultJsonProtocol

case class Number(
    length: Int,
    luhn: Boolean
)

case class Country(
    numeric: String,
    alpha2: String,
    name: String,
    emoji: String,
    currency: String,
    latitude: Int,
    longitude: Int
)

case class Bank(
    name: String,
    url: String,
    phone: String,
    city: String
)

case class BinInfo(
    number: Number,
    country: Country,
    bank: Bank,
    scheme: String,
    brand: String,
    `type`: String,
    prepaid: Boolean
)

object BinInfoJsonProtocol extends DefaultJsonProtocol {

  implicit val numberFormat  = jsonFormat2(Number.apply)
  implicit val countryFormat = jsonFormat7(Country.apply)
  implicit val bankFormat    = jsonFormat4(Bank.apply)
  implicit val binInfoFormat = jsonFormat7(BinInfo.apply)

}
