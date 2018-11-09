package ru.butik.butifactory

import io.circe.generic.auto._
import io.finch.circe._
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import io.circe.{Encoder, Json}
import io.finch._


object ApiEndpoints {
  def errorToJson(e: Error): Json = e match {
    case Error.NotPresent(_) =>
      Json.obj("error" -> Json.fromString("something_not_present"))
    case Error.NotParsed(_, _, _) =>
      Json.obj("error" -> Json.fromString("something_not_parsed"))
    case Error.NotValid(item, rule) =>
      Json.obj("error" -> Json.fromString(s"${item.description} $rule"))
  }

  implicit val ee: Encoder[Exception] = Encoder.instance {
    case e: Error => errorToJson(e)
    case Errors(nel) => Json.arr(nel.toList.map(errorToJson): _*)
  }


  def makeService(datastore: Datastore, frontend: ArtifactStorageFrontend, apkService: ApkService): Service[Request, Response] = (
    VersionsHandler.versions(datastore, frontend) :+:
      FileHandler.artifactUpload(apkService) :+:
      ArtifactsHandler.create(datastore) :+:
      frontend.fileServeHandler
    ).toService
}
