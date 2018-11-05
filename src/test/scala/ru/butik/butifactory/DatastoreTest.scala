package ru.butik.butifactory

import org.scalatest.FunSpec

class DatastoreTest extends FunSpec {

  it("should setup db") {
    val db = Datastore.init()

    val artifact = Artifact(0, "test")
    assert(db.createArtifact("test") === Artifact(1, "test"))
    assert(db.createArtifact("test") === Artifact(2, "test"))

    assert(db.findArtifact(1) === Some(artifact.copy(id = 1)))
    assert(db.findArtifact(100) === None)



    assert(db.artifacts() === List(artifact.copy(id = 1), artifact.copy(id = 2)))
  }

}
