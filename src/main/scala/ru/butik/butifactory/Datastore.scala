package ru.butik.butifactory
import cats.effect._
import doobie._
import doobie.implicits._

import scala.concurrent.ExecutionContext

case class Artifact(name: String)
case class ArtifactVersion(name: String, version: String, filename: String)

object Datastore {
  def init(): Datastore = {
    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    val xa = Transactor.fromDriverManager[IO]("org.h2.Driver", "jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1")
//    val xa = Transactor.fromDriverManager[IO]("org.h2.Driver", "jdbc:h2:~/test")

    new Datastore(xa)
  }
}

class Datastore(xa: Transactor.Aux[IO, Unit]) {
  def artifacts(): List[Artifact] = {
    sql"select name from artifacts".query[Artifact].to[List].transact(xa).unsafeRunSync
  }

  def createArtifact(name: String): Artifact = {
    val res = for {
      _ <- sql"insert into artifacts(name) values ($name)".update.run
      p  <- sql"select name from artifacts where name = $name".query[Artifact].unique
    } yield p
    res.transact(xa).unsafeRunSync()
  }

//  def createArtifact(name: String): Artifact = {
//    val res = for {
//      _  <- sql"insert into artifacts(name) values ($name)".update.run
//      id <- sql"select lastval()".query[Long].unique
//      p  <- sql"select id, name from artifacts where id = $id".query[Artifact].unique
//    } yield p
//    res.transact(xa).unsafeRunSync()
//  }

//  def saveArtifact(artifact: Artifact): Unit = {
//    sql"update artifacts set name = ${artifact.name} where id = ${artifact.id}".update.run.transact(xa).unsafeRunSync
//  }

  def findArtifactByName(name: String): Option[Artifact] = {
    sql"select name from artifacts where name = $name".query[Artifact].option.transact(xa).unsafeRunSync()
  }

  def findArtifactVersion(name: String, version: String): Option[ArtifactVersion] = {
    sql"select name, version, filename from artifacts_versions where name = $name and version = $version"
      .query[ArtifactVersion]
      .option
      .transact(xa)
      .unsafeRunSync()
  }

  def createArtifactVersion(name: String, version: String, filename: String): ArtifactVersion = {
    val res = for {
      _ <- sql"insert into artifacts_versions(name, version, filename) values ($name, $version, $filename)".update.run
      p  <- sql"select name, version, filename from artifacts_versions where name = $name and version = $version".query[ArtifactVersion].unique
    } yield p
    res.transact(xa).unsafeRunSync()
  }
}
