package ru.butik.butifactory

import io.circe.generic.auto._
import io.finch.circe._
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}

object Endpoint {
  def makeService(): Service[Request, Response] = (
    VersionsHandler.versions :+:
      FileHandler.artifactUpload
    ).toService
}
