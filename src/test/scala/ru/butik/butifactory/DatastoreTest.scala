package ru.butik.butifactory

import org.scalatest.FunSpec
import org.flywaydb.core.Flyway

class DatastoreTest extends FunSpec {

  it("should setup db") {

    val flyway: Flyway = Flyway.configure().dataSource("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", null, null).load
    flyway.migrate

    val db = Datastore.init()

    val artifact = Artifact(0, "test")
    assert(db.createArtifact("test") === Artifact(1, "test"))
    assert(db.createArtifact("test") === Artifact(2, "test"))

    assert(db.findArtifact(1) === Some(artifact.copy(id = 1)))
    assert(db.findArtifact(100) === None)



    assert(db.artifacts() === List(artifact.copy(id = 1), artifact.copy(id = 2)))
  }

}
