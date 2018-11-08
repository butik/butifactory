package ru.butik.butifactory

import java.io.File

import better.files._
import org.scalatest.{BeforeAndAfter, FunSpec}
import ru.butik.butifactory.ArtifactStorageBackend.DiskArtifactStorageBackend

class DiskArtifactStorageBackendTest extends FunSpec
  with BeforeAndAfter {

  private val testDataDir = "target" / "tmp"

  before {
    testDataDir.delete()
  }

  it("should save artifact") {

    val storage = new DiskArtifactStorageBackend("target/tmp")

    val apkFile = new File(Resource.getUrl("apk/app-release.apk").getFile)
    val path = "ru.butik.app/butik.apk"
    storage.storeArtifact("ru.butik.app/butik.apk", apkFile)

    val expectedPath = testDataDir / path
    assert(expectedPath.exists === true)

    assert(storage.artifactURI(path) === Right(path))
    assert(storage.artifactURI("nonexist") === Left("file not found"))
  }
}
