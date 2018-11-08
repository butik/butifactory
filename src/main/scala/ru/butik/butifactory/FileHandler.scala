package ru.butik.butifactory

import com.twitter.finagle.http.exp.Multipart
import io.finch._
import io.finch.syntax._
import com.twitter.finagle.http.exp.Multipart.FileUpload
import com.twitter.io.Buf.ByteBuffer
import net.dongliu.apk.parser.{ApkFile, ByteArrayApkFile}
import better.files._

import scala.util.control.NonFatal

object FileHandler {
  case class UploadResult(success: Boolean)

  def artifactUpload: Endpoint[UploadResult] =
    post("upload" :: multipartFileUpload("file")) { upload: FileUpload =>
      val res = try {
        val rs = for {
          apkFile <- (upload match {
            case Multipart.OnDiskFileUpload(file, contentType, fileName, contentTransferEncoding) =>
              new ApkFile(file)
            case Multipart.InMemoryFileUpload(buf, contentType, fileName, contentTransferEncoding) =>
              new ByteArrayApkFile(ByteBuffer.Owned.extract(buf).array())
          }).autoClosed
        } yield {
          println(apkFile.getApkMeta.getPackageName)
          Ok(UploadResult(true))
        }
        rs.get()
      } catch {
        case NonFatal(e) => BadRequest(new IllegalArgumentException("bad apk"))
      }

      res
    }
}
