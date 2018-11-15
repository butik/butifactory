package ru.butik.butifactory

import io.finch._
import io.finch.items.ParamItem
import io.finch.syntax._
import io.circe.generic.auto._
import io.finch.circe._

object SubscribeHandler {
  case class SubscribeResult(bundleName: String, result: Boolean)
  case class SubscribeRequest(bundleName: String, deviceId: String)

  def subscribe(db: Datastore): Endpoint[SubscribeResult] =
    post("subscribe" :: jsonBody[SubscribeRequest]) {
      request: SubscribeRequest =>
        db.findArtifactByName(request.bundleName) match {
          case None =>
            NotFound(Error.NotValid(ParamItem("name"), "name not found"))
          case Some(_) =>
            val res = db.addSubscription(request.bundleName, request.deviceId)
            Ok(SubscribeResult(bundleName = res.name, result = true))
        }
    }
}
