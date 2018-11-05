package ru.butik.butifactory

import io.finch.{Endpoint, Ok, jsonBody}
import io.finch.syntax.post

import io.finch._
import io.finch.circe._
import io.circe.generic.auto._

object TimeHandler {
  case class Locale(language: String, country: String)
  case class Time(locale: Locale, time: String)

  def currentTime(l: java.util.Locale): String =
    java.util.Calendar.getInstance(l).getTime.toString

  val time: Endpoint[Time] =
    post("time" :: jsonBody[Locale]) { l: Locale =>
      Ok(Time(l, currentTime(new java.util.Locale(l.language, l.country))))
    }
}
