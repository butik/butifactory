package ru.butik.butifactory

import com.twitter.finagle.http.Response
import com.twitter.finagle.service.Backoff
import com.twitter.finagle.{Http, http}
import com.twitter.io.Buf.ByteArray.Owned
import com.twitter.util.Future
import io.circe.{Encoder, Json}
import io.circe.generic.auto._
import io.circe.syntax._
import ru.butik.butifactory.PushService.{GoogleMessageFormat, GoogleMessageFormatPayload}

object PushService {
  case class GoogleMessageFormatPayload(messageType: String, artifactVersion: ArtifactVersion)
  case class GoogleMessageFormat(to: String, data: GoogleMessageFormatPayload, priority: String)

  implicit val encodeGoogleMessageFormatPayload: Encoder[GoogleMessageFormatPayload] = (a: GoogleMessageFormatPayload) =>
    Json.obj(
      ("type", Json.fromString(a.messageType)),
      ("artifactVersion", a.artifactVersion.asJson)
    )
}

class PushService(apiKey: String) {
  import com.twitter.conversions.time._
  private val twitter = Http.client
    .withRetryBackoff(Backoff.exponentialJittered(2.seconds, 60.seconds).take(10))
    .withTransport.tls("fcm.googleapis.com")
    .newService("fcm.googleapis.com:443")

  def pushDevice(deviceId: String, artifactVersion: ArtifactVersion): Future[Response] = {
    val request = http.Request(http.Method.Post, "/fcm/send")
    request.host = "fcm.googleapis.com"
    request.setContentTypeJson
    request.authorization = s"key=$apiKey"

    import PushService.encodeGoogleMessageFormatPayload
    val payload = GoogleMessageFormatPayload("NEWBUILD", artifactVersion)
    val message = GoogleMessageFormat(deviceId, payload, priority = "high").asJson.noSpaces
    request.content(Owned(message.getBytes))

    twitter(request)
  }
}
