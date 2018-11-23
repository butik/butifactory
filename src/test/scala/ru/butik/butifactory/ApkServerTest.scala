package ru.butik.butifactory

import java.io.File

import better.files.Resource
import com.twitter.util.Future
import net.dongliu.apk.parser.ApkFile
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FunSpec}
import ru.butik.butifactory.PushService.GoogleMessageResponse

class ApkServerTest extends FunSpec
  with BeforeAndAfter
  with MockFactory {

  private val apkFile = Resource.getUrl("apk/app-release.apk").getFile

  private val datastore = mock[Datastore]
  private val storage = mock[ArtifactStorageBackend]
  private val frontend = mock[ArtifactStorageFrontend]
  private val pushService = mock[PushService]
  private val service = new ApkService(datastore, storage, frontend, pushService)
  private val apk = new ApkFile(apkFile)
  private val apkContainer = ApkFileContainer(apk, new File(apkFile))

  it("should check for artifact") {
    (datastore.findArtifactByName _).expects(*).returning(None)

    assert(service.uploadFile(apkContainer) === Left("artifact not found"))
  }

  it("should check for version") {
    (datastore.findArtifactByName _).expects(*).returning(Some(Artifact("name")))
    (datastore.findArtifactVersion _).expects(*, *).returning(Some(ArtifactVersion("name", "1.2.3", 6, "file")))

    assert(service.uploadFile(apkContainer) === Left("already exist"))
  }

  it("should create artifact and store in storage") {
    val version = ArtifactVersion("ru.butik.fitassist", "1.1.3", 6, "ru.butik.fitassist/ru.butik.fitassist-6.apk")
    val expect = ArtifactVersionAndroid(version.version, version.versionCode, "http://test.ru/abc")

    (datastore.findArtifactByName _).expects(*).returning(Some(Artifact("name")))
    (datastore.findArtifactVersion _).expects(*, *).returning(None)
    (datastore.createArtifactVersion _)
      .expects(version.name, version.version, version.versionCode, version.filename)
      .returning(version)
    (datastore.fetchSubscriptions _).expects(version.name).returns(List(Subscription(version.name, "123")))
    (pushService.pushDevice _).expects("123", *).returns(Future { Right(GoogleMessageResponse(1, 0, 0)) })

    (storage.storeArtifact _).expects(version.filename, apkContainer.file)
    (frontend.pathToURL _).expects("ru.butik.fitassist/ru.butik.fitassist-6.apk").returns(expect.url)

    assert(service.uploadFile(apkContainer) === Right(version))
  }

  it("should remove subscription if error from GCM") {
    val version = ArtifactVersion("ru.butik.fitassist", "1.1.3", 6, "ru.butik.fitassist/ru.butik.fitassist-6.apk")
    val expect = ArtifactVersionAndroid(version.version, version.versionCode, "http://test.ru/abc")

    (datastore.findArtifactByName _).expects(*).returning(Some(Artifact("name")))
    (datastore.findArtifactVersion _).expects(*, *).returning(None)
    (datastore.createArtifactVersion _)
      .expects(version.name, version.version, version.versionCode, version.filename)
      .returning(version)
    (datastore.fetchSubscriptions _).expects(version.name).returns(List(Subscription(version.name, "123")))
    (pushService.pushDevice _).expects("123", *).returns(Future { Left("error") })

    (datastore.removeSubscription _).expects(version.name, "123").returns(1)

    (storage.storeArtifact _).expects(version.filename, apkContainer.file)
    (frontend.pathToURL _).expects("ru.butik.fitassist/ru.butik.fitassist-6.apk").returns(expect.url)

    assert(service.uploadFile(apkContainer) === Right(version))
  }
}
