package ru.butik.butifactory

import java.io.{Closeable, File, FileInputStream}
import java.security.{DigestInputStream, MessageDigest}

import net.dongliu.apk.parser.AbstractApkFile

case class ApkFileContainer(apkFile: AbstractApkFile, file: File) extends Closeable {
  override def close(): Unit = {
    apkFile.close()
  }
}

class ApkService(datastore: Datastore, storageBackend: ArtifactStorageBackend, frontend: ArtifactStorageFrontend, pushService: PushService) {
  val bufferSize = 8192

  def uploadFile(apk: ApkFileContainer): Either[String, ArtifactVersion] = {
    val artifactName = apkToName(apk.apkFile)
    val version = apk.apkFile.getApkMeta.getVersionName
    val versionCode = apk.apkFile.getApkMeta.getVersionCode

    val buffer = new Array[Byte](bufferSize)
    val md5Instance = MessageDigest.getInstance("MD5")
    val dis = new DigestInputStream(new FileInputStream(apk.file), md5Instance)
    try {
      while (dis.read(buffer) != -1) { }
    } finally {
      try { dis.close() } catch { case _: Throwable => }
    }
    val md5 = Option(md5Instance.digest.map("%02x".format(_)).mkString)

    datastore.findArtifactByName(artifactName) match {
      case None =>
        Left("artifact not found")
      case Some(_) =>
        datastore.findArtifactVersion(artifactName, versionCode) match {
          case None =>
            val filename = apkToFilename(apk.apkFile)
            storageBackend.storeArtifact(filename, apk.file)
            val artifactVersion = datastore.createArtifactVersion(artifactName, version, versionCode, filename, md5)
            notifyDevices(artifactVersion)
            Right(artifactVersion)
          case Some(_) => Left("already exist")
        }
    }
  }

  def notifyDevices(version: ArtifactVersion): Unit = {
    val artifactVersion = ArtifactVersionAndroid(version.name, version.version, version.versionCode, frontend.pathToURL(version.filename), version.md5)
    datastore.fetchSubscriptions(version.name).foreach { subscription =>
      pushService.pushDevice(subscription.deviceId, artifactVersion).foreach {
        case Left(_) =>
          val _ = datastore.removeSubscription(version.name, subscription.deviceId)
        case Right(_) =>
      }
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
