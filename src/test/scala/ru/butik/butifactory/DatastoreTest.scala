package ru.butik.butifactory

import cats.effect.IO
import org.scalatest.{BeforeAndAfterEach, FunSpec, Matchers}
import doobie.implicits._
import doobie.scalatest._
import org.flywaydb.core.Flyway

class DatastoreTest extends FunSpec
  with Matchers
  with IOChecker
  with BeforeAndAfterEach {

  val flyway: Flyway = Flyway.configure().dataSource(DatastoreDoobie.testUrl, null, null).load
  flyway.migrate

  private val db = DatastoreDoobie.init(DatastoreDoobie.testUrl)

  override def transactor: doobie.Transactor[IO] = db.xa

  override protected def beforeEach(): Unit = {
    {
      val _ = sql"TRUNCATE TABLE subscription".update.run.transact(db.xa).unsafeRunSync()
    }
    {
      val _ = sql"TRUNCATE TABLE artifacts_versions".update.run.transact(db.xa).unsafeRunSync()
    }
  }

  it("should save artifacts") {
    val artifact = Artifact("test")
    assert(db.createArtifact("test") === Artifact("test"))

    assert(db.findArtifactByName("test") === Some(artifact))
    assert(db.findArtifactByName("not exists") === None)

    assert(db.artifacts() === List(artifact))
  }

  it("checkInsertArtifactVersion") {
    check(db.insertArtifactVersion("", "", 6, "", Option("")))
  }
  it("artifactVersionQueryByNameAndVersionCode") {
    check(db.artifactVersionQuery("", 6))
  }
  it("artifactVersionQueryByName") {
    check(db.artifactVersionQuery(""))
  }
  it("artifactsQuery") {
    check(db.artifactsQuery())
  }
  it("artifactQuery") {
    check(db.artifactQuery(""))
  }
  it("insertArtifact") {
    check(db.insertArtifact(""))
  }
  it("insertSubscription") {
    check(db.insertSubscriptionQuery("", ""))
  }
  it("deleteSubscription") {
    check(db.deleteSubscriptionQuery("", ""))
  }
  it("findSubscription") {
    check(db.findSubscriptionQuery("", ""))
  }
  it("findSubscriptions") {
    check(db.findSubscriptionsQuery(""))
  }

  it("should save artifact versions") {
    val artifact = "test"
    val version = "1.2.3+Test"
    val filename = "apktest.apk"
    val md5 = Option("1e6566b09c7b5a41612d804330a8988c")
    val expected = ArtifactVersion(artifact, version, 6, filename, md5)

    assert(db.createArtifactVersion(artifact, version, expected.versionCode, filename, md5) === expected)
    assert(db.findArtifactVersion(artifact, expected.versionCode) === Some(expected))
    assert(db.findArtifactVersion("unknown", expected.versionCode) === None)
  }

  it("should fetch artifact versions") {
    val artifact = "test.one"
    val version = "1.2.3+Test"
    val filename = "apktest.apk"
    val md5 = Option("1e6566b09c7b5a41612d804330a8988c")
    val expected = ArtifactVersion(artifact, version, 6, filename, md5)

    assert(db.createArtifactVersion(artifact, version, expected.versionCode, filename, md5) === expected)
    assert(db.findVersionsBy("test.one") === List(expected))
  }

  it("should save subscription") {
    val expected = Subscription("name", "123")

    assert(db.addSubscription(expected.name, expected.deviceId) === expected)
    assert(db.addSubscription(expected.name, expected.deviceId) === expected)
    assert(db.fetchSubscriptions(expected.name) === List(expected))
  }

  it("should remove subscription") {
    val expected = Subscription("name", "123")

    assert(db.addSubscription(expected.name, expected.deviceId) === expected)
    assert(db.fetchSubscriptions(expected.name) === List(expected))
    assert(db.removeSubscription(expected.name, expected.deviceId) === 1)
    assert(db.fetchSubscriptions(expected.name) === List())
  }
}
