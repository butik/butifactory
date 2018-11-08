package ru.butik.butifactory

import io.finch.{Endpoint, Ok, path}
import io.finch._
import io.finch.items.ParamItem
import io.finch.syntax._

object ArtifactsHandler {
  def create(db: Datastore): Endpoint[Artifact] =
    post("artifacts" :: path[String] :: path[String]) {
      (group: String, name: String) =>
        db.findArtifactByName(s"$group.$name") match {
          case None =>
            val artifact = db.createArtifact(s"$group.$name")
            Ok(artifact)
          case Some(_) =>
            Conflict(Error.NotValid(ParamItem("group"), "already exists"))
        }
    }
}
