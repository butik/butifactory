package ru.butik.butifactory

import io.finch._
import org.scalamock.scalatest.MockFactory
import org.scalatest.FunSpec

class VersionHanderTest extends FunSpec
  with MockFactory{

  it("should respond with versionCode ordered") {
    val frontendStorage = mock[ArtifactStorageFrontend]
    val url2 = "http://butifactory.ru/data/test_url2"
    val url4 = "http://butifactory.ru/data/test_url4"
    (frontendStorage.pathToURL _).expects("test_url2").returns(url2)
    (frontendStorage.pathToURL _).expects("test_url4").returns(url4)

    val datastore = mock[Datastore]
    (datastore.findVersionsBy _).expects("group.name").returns(
      List(
        ArtifactVersion("group.name", "", 5, "test_url2"),
        ArtifactVersion("group.name", "", 6, "test_url4")
      )
    )

    val Some(response) = VersionsHandler.versions(datastore, frontendStorage)(Input.get("/versions/group.name")).awaitValueUnsafe()

    assert(response.bundleName === "group.name")
    assert(response.versions === List(
      ArtifactVersionAndroid("", 6, url4),
      ArtifactVersionAndroid("", 5, url2)
    ))
  }
}
