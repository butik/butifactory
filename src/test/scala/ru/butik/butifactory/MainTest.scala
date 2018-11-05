package ru.butik.butifactory

import io.finch._
import io.finch.circe._
import io.circe.generic.auto._
import org.scalatest.FunSpec

class MainTest extends FunSpec {
  it("should get route") {
    assert(
      TimeHandler.time(
        Input
          .post("/time")
          .withBody[Application.Json](TimeHandler.Locale(language = "ru", country = "ru")))
        .awaitValueUnsafe() === "test")
  }
}
