package ru.butik.butifactory

import io.finch._
import io.finch.syntax._

object VersionsHandler {
  case class Versions(bundleName: String, versions: List[ArtifactVersionAndroid])

  def versions(db: Datastore, frontend: ArtifactStorageFrontend): Endpoint[Versions] =
    get("versions" :: path[String]) {
      group: String =>
        val versions = db.findVersionsBy(group)
          .sortWith{ (v1, v2) =>
            v1.versionCode > v2.versionCode }
          .map { version =>
            ArtifactVersionAndroid(version.version, version.versionCode, frontend.pathToURL(version.filename))
          }
        Ok(Versions(bundleName = s"$group", versions = versions))
    }
}
