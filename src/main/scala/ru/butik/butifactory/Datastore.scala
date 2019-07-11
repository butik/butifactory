package ru.butik.butifactory

trait Datastore {
  def artifacts(): List[Artifact]
  def createArtifact(name: String): Artifact
  def findArtifactByName(name: String): Option[Artifact]
  def findArtifactVersion(name: String, versionCode: Long): Option[ArtifactVersion]
  def createArtifactVersion(name: String, version: String, versionCode: Long, filename: String, md5: Option[String]): ArtifactVersion
  def findVersionsBy(group: String): List[ArtifactVersion]
  def addSubscription(name: String, deviceId: String): Subscription
  def removeSubscription(name: String, deviceId: String): Int
  def fetchSubscriptions(name: String): List[Subscription]
}
