package ru.butik.butifactory

import com.twitter.finagle.Http
import com.twitter.util.Await

import io.finch.circe._
import io.circe.generic.auto._

object Main extends App {
  Await.ready(Http.server.serve(":8081", VersionsHandler.versions.toService))
}
