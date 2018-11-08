package ru.butik.butifactory

trait Datastore {
  def artifacts(): List[Artifact]
  def createArtifact(name: String): Artifact
  def findArtifactByName(name: String): Option[Artifact]
  def findArtifactVersion(name: String, version: String): Option[ArtifactVersion]
  def createArtifactVersion(name: String, version: String, filename: String): ArtifactVersion
  def findVersionsBy(group: String, name: String): List[ArtifactVersion]
}
