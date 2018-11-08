package ru.butik.butifactory

import com.twitter.finagle.Http
import com.twitter.util.Await

object Main extends App {
  Await.ready(
    Http.server.serve(
      addr = ":8081",
      Endpoint.makeService()))
}
