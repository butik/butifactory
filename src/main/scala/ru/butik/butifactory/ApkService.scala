package ru.butik.butifactory

import java.io.{Closeable, File}

import net.dongliu.apk.parser.AbstractApkFile

case class ApkFileContainer(apkFile: AbstractApkFile, file: File) extends Closeable {
  override def close(): Unit = {
    apkFile.close()
  }
}

class ApkService(datastore: Datastore, storageBackend: ArtifactStorageBackend) {

  def uploadFile(apk: ApkFileContainer): Either[String, ArtifactVersion] = {
    val artifactName = apkToName(apk.apkFile)
    val version = apk.apkFile.getApkMeta.getVersionName

    datastore.findArtifactByName(artifactName) match {
      case None =>
        Left("artifact not found")
      case Some(_) =>
        datastore.findArtifactVersion(artifactName, version) match {
          case None =>
            val filename = apkToFilename(apk.apkFile)
            storageBackend.storeArtifact(filename, apk.file)
            Right(datastore.createArtifactVersion(artifactName, version, filename))
          case Some(_) => Left("already exist")
        }
    }
  }

  def apkToName(apk: AbstractApkFile): String = {
    val meta = apk.getApkMeta
    s"${meta.getPackageName}.${meta.getName}"
  }

  def apkToFilename(apk: AbstractApkFile): String = {
    val meta = apk.getApkMeta
    s"${meta.getPackageName}/${meta.getName}-${meta.getVersionName}.apk"
  }
}
