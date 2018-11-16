package ru.butik.butifactory

import java.io.{Closeable, File}

import net.dongliu.apk.parser.AbstractApkFile

case class ApkFileContainer(apkFile: AbstractApkFile, file: File) extends Closeable {
  override def close(): Unit = {
    apkFile.close()
  }
}

class ApkService(datastore: Datastore, storageBackend: ArtifactStorageBackend, frontend: ArtifactStorageFrontend, pushService: PushService) {

  def uploadFile(apk: ApkFileContainer): Either[String, ArtifactVersion] = {
    val artifactName = apkToName(apk.apkFile)
    val version = apk.apkFile.getApkMeta.getVersionName
    val versionCode = apk.apkFile.getApkMeta.getVersionCode

    datastore.findArtifactByName(artifactName) match {
      case None =>
        Left("artifact not found")
      case Some(_) =>
        datastore.findArtifactVersion(artifactName, versionCode) match {
          case None =>
            val filename = apkToFilename(apk.apkFile)
            storageBackend.storeArtifact(filename, apk.file)
            val artifactVersion = datastore.createArtifactVersion(artifactName, version, versionCode, filename)
            notifyDevices(artifactVersion)
            Right(artifactVersion)
          case Some(_) => Left("already exist")
        }
    }
  }

  def notifyDevices(version: ArtifactVersion): Unit = {
    val artifactVersion = ArtifactVersionAndroid(version.version, version.versionCode, frontend.pathToURL(version.filename))
    datastore.fetchSubscriptions(version.name).foreach { subscription =>
      pushService.pushDevice(subscription.deviceId, artifactVersion)
    }
  }

  def apkToName(apk: AbstractApkFile): String = {
    val meta = apk.getApkMeta
    s"${meta.getPackageName}"
  }

  def apkToFilename(apk: AbstractApkFile): String = {
    val meta = apk.getApkMeta
    s"${meta.getPackageName}/${meta.getPackageName}-${meta.getVersionCode}.apk"
  }
}
