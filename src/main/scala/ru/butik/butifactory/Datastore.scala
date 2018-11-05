package ru.butik.butifactory
import cats.effect._
import doobie._
import doobie.implicits._

import scala.concurrent.ExecutionContext

case class Artifact(id: Int, name: String)

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
    sql"select id, name from artifacts".query[Artifact].to[List].transact(xa).unsafeRunSync
  }

  def createArtifact(name: String): Artifact = {
    val res = for {
      _  <- sql"insert into artifacts(name) values ($name)".update.run
      id <- sql"select lastval()".query[Long].unique
      p  <- sql"select id, name from artifacts where id = $id".query[Artifact].unique
    } yield p
    res.transact(xa).unsafeRunSync()
  }

  def saveArtifact(artifact: Artifact): Unit = {
    sql"update artifacts set name = ${artifact.name} where id = ${artifact.id}".update.run.transact(xa).unsafeRunSync
  }

  def findArtifact(id: Int): Option[Artifact] = {
    sql"select id, name from artifacts where id = $id".query[Artifact].option.transact(xa).unsafeRunSync()
  }
}
