package ru.butik.butifactory

import com.gilt.gfc.semver.SemVer
import io.finch._
import io.finch.syntax._

object VersionsHandler {
  case class Versions(bundleName: String, versions: List[ArtifactVersionAndroid])

  def versions(db: Datastore, frontend: ArtifactStorageFrontend): Endpoint[Versions] =
    get("versions" :: path[String] :: path[String]) {
      (group: String, name: String) =>
        val versions = db.findVersionsBy(group, name)
          .sortWith{ (v1, v2) =>
            SemVer(v1.version) > SemVer(v2.version) }
          .map { version =>
            ArtifactVersionAndroid(version.version, frontend.pathToURL(version.filename))
          }
        Ok(Versions(bundleName = s"$group.$name", versions = versions))
    }
}
