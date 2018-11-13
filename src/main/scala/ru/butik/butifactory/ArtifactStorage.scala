package ru.butik.butifactory

import java.io.File

import better.files._

import better.files.{File => ScalaFile, _}
import com.twitter.concurrent.AsyncStream
import com.twitter.io.{Buf, Reader}
import io.finch._
import io.finch.syntax._
import org.apache.commons.io.FilenameUtils

trait ArtifactStorage {
  def storeArtifact(path: String, file: File): Unit
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
  def storeArtifact(path: String, file: File): ScalaFile
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

    def fromReader(reader: Reader[Buf]): AsyncStream[Buf] =
      AsyncStream.fromFuture(reader.read(Int.MaxValue)).flatMap {
        case None => AsyncStream.empty
        case Some(a) => a +:: fromReader(reader)
      }

    val fileServeHandler: Endpoint[AsyncStream[Buf]] = get("data" :: paths[String]) { paths: Seq[String] =>
      val reader: Reader[Buf] = Reader.fromFile((dataDir / FilenameUtils.normalizeNoEndSeparator(paths.mkString("/"))).toJava)
      Ok(AsyncStream.fromFuture(reader.read(Int.MaxValue)).flatMap {
        case None => AsyncStream.empty
        case Some(a) => a +:: fromReader(reader)
      })
      //  .fromReader(reader, chunkSize = 512.kilobytes.inBytes.toInt)
    }
  }

  class DiskArtifactStorageBackend(dir: String) extends ArtifactStorageBackend{
    val directory = new File(dir)
    if (!directory.exists()) {
      directory.mkdirs()
    }

    override def storeArtifact(path: String, file: File): ScalaFile = {
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
