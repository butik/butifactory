package ru.butik.butifactory

import java.io.FileOutputStream

import com.twitter.finagle.http.exp.Multipart
import io.finch._
import io.finch.syntax._
import io.finch.circe._
import com.twitter.finagle.http.exp.Multipart.FileUpload
import com.twitter.io.Buf.ByteBuffer
import net.dongliu.apk.parser.{ApkFile, ByteArrayApkFile}
import better.files._
import io.finch.items.{ParamItem, RequestItem}

import scala.util.control.NonFatal

object FileHandler {
  case class UploadResult(success: Boolean)

  def artifactUpload(apkService: ApkService): Endpoint[UploadResult] =
    post("upload" :: multipartFileUpload("file")) { upload: FileUpload =>
      val res = try {
        val rs = for {
          apkFile <- (upload match {
            case Multipart.OnDiskFileUpload(file, contentType, fileName, contentTransferEncoding) =>
              ApkFileContainer(new ApkFile(file), file)
            case Multipart.InMemoryFileUpload(buf, contentType, fileName, contentTransferEncoding) =>
              // todo: file leak
              val file = File.newTemporaryFile()
              val outputChannel = new FileOutputStream(file.toJava).getChannel
              outputChannel.write(ByteBuffer.Owned.extract(buf))
              outputChannel.close()
              ApkFileContainer(new ByteArrayApkFile(ByteBuffer.Owned.extract(buf).array()), file.toJava)

          }).autoClosed
        } yield {
          apkService.uploadFile(apkFile) match {
            case Left(err) =>
              println(err)
              Conflict(Error.NotValid(ParamItem("file"), err))
            case Right(artifactVersion) =>
              Ok(UploadResult(true))
          }
        }
        rs.get()
      } catch {
        case NonFatal(e) => BadRequest(new IllegalArgumentException("bad apk"))
      }

      res
    }
}
