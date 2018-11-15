package ru.butik.butifactory

import com.twitter.util.Await
import org.scalatest.FunSpec

class PushTest extends FunSpec{

  ignore("should push") {
    val pushService = new PushService("")

    val deviceId = ""
    val artifactVersion = ArtifactVersion("test", "1.1.1", 8, "http://test.com")
    val fut = pushService.pushDevice(deviceId, artifactVersion).onSuccess { r =>
      println(r)
      println("ok")
    }.onFailure { ex =>
      ex.printStackTrace()
    }

    Await.result(fut)
  }

}
