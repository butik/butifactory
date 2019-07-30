package ru.butik.butifactory

import com.twitter.finagle.service.Backoff
import com.twitter.finagle.{Http, http}
import com.twitter.io.Buf.ByteArray.Owned
import com.twitter.util.Future
import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import ru.butik.butifactory.PushService.{GoogleMessageFormat, GoogleMessageFormatPayload, GoogleMessageResponse}

object PushService {
  case class GoogleMessageFormatPayload(messageType: String, artifactVersion: ArtifactVersionAndroid)
  case class GoogleMessageFormat(to: String, data: GoogleMessageFormatPayload, priority: String)

  implicit val encodeGoogleMessageFormatPayload: Encoder[GoogleMessageFormatPayload] = (a: GoogleMessageFormatPayload) =>
    Json.obj(
      ("type", Json.fromString(a.messageType)),
      ("artifactVersion", a.artifactVersion.asJson)
    )

  case class GoogleMessageResponse(multicastId: Long, success: Int, failure: Int)
  implicit val decodeGoogleMessageResponse: Decoder[GoogleMessageResponse] = new Decoder[GoogleMessageResponse] {
    override def apply(c: HCursor): Result[GoogleMessageResponse] = {
      for {
        multicastId <- c.downField("multicast_id").as[Long]
        success <- c.downField("success").as[Int]
        failure <- c.downField("failure").as[Int]
      } yield {
        GoogleMessageResponse(multicastId, success, failure)
      }
    }
  }
}

class PushService(config: Config) {
  import com.twitter.conversions.time._
  private val twitter = Http.client
    .withRetryBackoff(Backoff.exponentialJittered(2.seconds, 60.seconds).take(10))
    .withTransport.tls(config.pushHost)
    .newService(s"${config.pushHost}:${config.pushPort}")

  def pushDevice(deviceId: String, artifactVersion: ArtifactVersionAndroid): Future[Either[String, GoogleMessageResponse]] = {
    val request = http.Request(http.Method.Post, config.pushMethod)
    request.host = config.pushHost
    request.setContentTypeJson
    request.authorization = s"key=${config.pushKey}"

    import PushService.encodeGoogleMessageFormatPayload
    val payload = GoogleMessageFormatPayload("NEWBUILD", artifactVersion)
    val message = GoogleMessageFormat(deviceId, payload, priority = "high").asJson.noSpaces
    request.content(Owned(message.getBytes))

    twitter(request).flatMap { response =>
      import PushService._
      import io.circe.parser._
      val content = response.contentString
      Future.value(parse(content) match {
        case Left(err) => Left(err.message)
        case Right(json) => json.as[GoogleMessageResponse] match {
          case Left(err) => Left(err.message)
          case Right(googleMessage) => Right(googleMessage)
        }
      })
    }
  }
}
