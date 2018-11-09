package ru.butik.butifactory

import java.io.File

import better.files._
import File._

import better.files.{File => ScalaFile, _}
import com.twitter.concurrent.AsyncStream
import com.twitter.io.{Buf, Reader}
import io.finch._
import io.finch.syntax._
import com.twitter.conversions.storage._
import org.apache.commons.io.FilenameUtils

trait ArtifactStorage {
  def storeArtifact(path: String, file: File)
  def uri(path: String): Either[String, String]
}

class ArtifactStorageImpl(backend: ArtifactStorageBackend, frontend: ArtifactStorageFrontend) extends ArtifactStorage {
  override def storeArtifact(path: String, file: File): Unit = ???

  override def uri(path: String): Either[String, String] = {
    backend.artifactURI(path).flatMap { uri =>
      Right(frontend.pathToURL(uri))
    }
  }
}

trait ArtifactStorageBackend {
  def storeArtifact(path: String, file: File)
  def artifactURI(path: String): Either[String, String]
}

trait ArtifactStorageFrontend {
  def pathToURL(path: String): String
  val fileServeHandler: Endpoint[AsyncStream[Buf]]
}

object ArtifactStorageBackend {

  class SelfHostedHTTPArtifactStorageFrontend(host: String, dataDir: String) extends ArtifactStorageFrontend {
    override def pathToURL(path: String): String = {
      host + path
    }

    val fileServeHandler: Endpoint[AsyncStream[Buf]] = get(dataDir :: paths[String]) { paths: Seq[String] =>
      val reader: Reader = Reader.fromFile((dataDir / FilenameUtils.normalizeNoEndSeparator(paths.mkString("/"))).toJava)
      Ok(AsyncStream.fromReader(reader, chunkSize = 512.kilobytes.inBytes.toInt))
    }
  }

  class DiskArtifactStorageBackend(dir: String) extends ArtifactStorageBackend{
    val directory = new File(dir)
    if (!directory.exists()) {
      directory.mkdirs()
    }

    override def storeArtifact(path: String, file: File): Unit = {
      (dir / path).parent.createDirectories()
      file.toScala.copyTo(dir / path)
    }

    override def artifactURI(path: String): Either[String, String] = {
      val file = dir / path
      if (!file.exists()) {
        Left("file not found")
      } else {
        Right(path)
      }
    }
  }
}
