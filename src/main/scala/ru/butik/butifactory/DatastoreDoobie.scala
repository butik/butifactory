package ru.butik.butifactory
import cats.effect._
import doobie._
import doobie.implicits._
import doobie.util.yolo

import scala.concurrent.ExecutionContext

case class Artifact(name: String)
case class ArtifactVersion(name: String, version: String, versionCode: Long, filename: String)

object DatastoreDoobie {
  val testUrl = "jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1"

  def init(url: String): DatastoreDoobie = {
    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    val xa = Transactor.fromDriverManager[IO]("org.h2.Driver", url)

    new DatastoreDoobie(xa)
  }
}

class DatastoreDoobie(val xa: Transactor.Aux[IO, Unit]) extends Datastore {
  val yoloDB: yolo.Yolo[IO] = xa.yolo

  def artifactsQuery(): doobie.Query0[Artifact] = sql"select name from artifacts".query[Artifact]
  def insertArtifactVersion(name: String, version: String, versionCode: Long, filename: String): doobie.Update0 =
    sql"insert into artifacts_versions(name, version, version_code, filename) values ($name, $version, $versionCode, $filename)".update
  def artifactVersionQuery(name: String, versionCode: Long): doobie.Query0[ArtifactVersion] =
    sql"select name, version, version_code, filename from artifacts_versions where name = $name and version_code = $versionCode".query[ArtifactVersion]
  def artifactVersionQuery(name: String): doobie.Query0[ArtifactVersion] =
    sql"select name, version, version_code, filename from artifacts_versions where name = $name".query[ArtifactVersion]
  def artifactQuery(name: String): doobie.Query0[Artifact] =
    sql"select name from artifacts where name = $name".query[Artifact]
  def insertArtifact(name: String): doobie.Update0 =
    sql"insert into artifacts(name) values ($name)".update

  override def artifacts(): List[Artifact] = {
    artifactsQuery().to[List].transact(xa).unsafeRunSync
  }

  override def createArtifact(name: String): Artifact = {
    val res = for {
      _ <- insertArtifact(name).run
      p <- artifactQuery(name).unique
    } yield p
    res.transact(xa).unsafeRunSync()
  }

  override def findArtifactByName(name: String): Option[Artifact] = {
    artifactQuery(name).option.transact(xa).unsafeRunSync()
  }

  override def findArtifactVersion(name: String, versionCode: Long): Option[ArtifactVersion] = {
    artifactVersionQuery(name, versionCode)
      .option
      .transact(xa)
      .unsafeRunSync()
  }

  override def createArtifactVersion(name: String, version: String, versionCode: Long, filename: String): ArtifactVersion = {
    val res = for {
      _ <- insertArtifactVersion(name, version, versionCode, filename).run
      p <- artifactVersionQuery(name, versionCode).unique
    } yield p
    res.transact(xa).unsafeRunSync()
  }

  override def findVersionsBy(group: String): List[ArtifactVersion] = {
    artifactVersionQuery(group).to[List].transact(xa).unsafeRunSync
  }
}
