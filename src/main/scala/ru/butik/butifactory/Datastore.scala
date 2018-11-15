package ru.butik.butifactory

trait Datastore {
  def artifacts(): List[Artifact]
  def createArtifact(name: String): Artifact
  def findArtifactByName(name: String): Option[Artifact]
  def findArtifactVersion(name: String, versionCode: Long): Option[ArtifactVersion]
  def createArtifactVersion(name: String, version: String, versionCode: Long, filename: String): ArtifactVersion
  def findVersionsBy(group: String): List[ArtifactVersion]
}
