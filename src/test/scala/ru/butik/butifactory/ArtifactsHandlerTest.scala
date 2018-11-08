package ru.butik.butifactory

import io.finch.{Error, Input}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers}

class ArtifactsHandlerTest extends FunSpec
  with Matchers
  with MockFactory {

  it("should reject if artifact exist") {
    val datastore = mock[Datastore]
    (datastore.findArtifactByName _).expects(*).returns(
      Some(Artifact("test"))
    )

    a[Error.NotValid] shouldBe thrownBy(ArtifactsHandler.create(datastore)(Input.post("/artifacts/group/name")).awaitValueUnsafe())
  }

  it("should create new artifact") {
    val datastore = mock[Datastore]
    (datastore.findArtifactByName _).expects(*).returns(
      None
    )
    (datastore.createArtifact _).expects("group.name").returns(
      Artifact("group.name")
    )

    val Some(response) = ArtifactsHandler.create(datastore)(Input.post("/artifacts/group/name")).awaitValueUnsafe()

    assert(response.name === "group.name")
  }
}
