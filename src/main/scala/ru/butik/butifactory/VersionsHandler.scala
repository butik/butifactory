package ru.butik.butifactory

import io.finch._
import io.finch.syntax._

object VersionsHandler {
  case class Versions(bundleName: String, versions: List[ArtifactVersionAndroid])

  def versions: Endpoint[Versions] =
    get("versions") {
      val result = List(
        ArtifactVersionAndroid(6, "1.1.3")
      )
      Ok(Versions(bundleName = "ru.butik.apk", versions = result))
    }
}
