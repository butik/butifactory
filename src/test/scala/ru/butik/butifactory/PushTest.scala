package ru.butik.butifactory

import com.twitter.util.Await
import org.scalatest.FunSpec

class PushTest extends FunSpec{

  ignore("should push") {
    val pushService = new PushService("AIzaSyDn-U0alpwS82txxw-z-GIMRusvjEwjZXE")

//    val deviceId = "fs_TplPeunc:APA91bHVpYV7CkmGo8cx4z4cwE-8HYAXUt-NjXecmuov435Nm-RUu0HWk7uyUT0eDNtu7KKKLnnM2QDbJl4nxinRs_lf11w18JXOCR9LMqovRTOC0SoMIZeFtNaBpKrF7J6yzkZZgjjL"
    val artifactVersion = ArtifactVersionAndroid("1.1.1", 8, "http://test.com")

    val devices = List("cLENzg3Y1dA:APA91bEBe8MUgL5-izwKGddZtqes2Yfw-z1EUdP2eoJHOOl4INKOF_z2-jijFitOoMZZgPaJ3--h0bWDnOyMkUFeCaawkcn-bX51ZmIWSdAyf1fSNyNH5Bc3g0NWaUS4YkBwH_NbZJvu","cf0mQ9AEaSY:APA91bE0PQ7DHmcUWisU9eVct_eJBsWrC0RBwpLlJ5daBr9kvIVO4H0zVxgPKiHphZTnELea1rij-vlyhjMUZQguxVnu-psm-t8sxyJVwzNF5SqW5EBunNvIUaix55hsh-Kz3p5sIIoY","cha3yP8fC2M:APA91bG1sIC4rkg_TXkZHQ_JOn9IGo3fs1WzeDoBvxudzW8n0XL8wLrl0qQmWSSttcm0NaWzf2kD0Q-MSyDTfUN0_GpNfrqyQ-fuhIQ_1ZODkHMwSF-VE4IvJ1H1z-QpC6f4kpGJsrki","crmOJCt6p3Y:APA91bEeEyLJnGpnVQHMefvUJbg3_a3Q_9dQiltIvnVnZVlFSeSxAa6PNaarq9gapHhlOZt5l94FLe6i2EUuTnuoe3XftdOpP9JSigWgq1FNTt0ZjfRjetibf1k7z90sOpgbOi2IvAu3","d6kniZXoiY4:APA91bFqUVr4OdxWxetDR-u-ksxdLcHOLnQkcC0Lm1dxbX4B7W1Jz77hXpK93OhxGEsWKvjRX3KRlqE8KPn8NUaBBXjj4NjelYLoZnn8GyZvEm6NZYZKeF0zeovWfS7cYZMlFeSJeUbO","dDcx1q6BUhI:APA91bHv8Mth8EzCBuiDbSW1qcG_2D_lWPr7JQL0TIE_H8VNvss9GUkAKDSDmtC-m1MkMGqFs_xUEHkM7d-tqv18m7faevYOSP0fFxZkuE4_GVheSK9wjlcDfo5wxbGFesK8eeOKGTkd","dZ_G7PGMbcc:APA91bGQbL3KX0aOtUCsJ8oD4_6IaKWwMGJqGj-VXR7oE3bsSvfuwYXJUfMi48pgS57j2XDs2qNnxFNJbeh8s19YzqG0CEnJOvAvO3ggJgLbf7BSIOg33ddlWXgW0WTiot3D2OKg4ABf","dluuRb49QGI:APA91bHzb5cufSFf_64pAWMSXYlkj_QWsZIkazSjnOqMOLPURvpzHMKj8-15irB_rZaBV-syirG9JFlx1P3VCcBSvP80_Ie2Jid5oqPNrOH2z5oaPb9Vy9CjSlGaV7gYY6P7ctHVOV_8","duSKpDo22Bk:APA91bFswa3QY8oe0mWOr2G1FcUlwugz_4w9LwbNHVXQb-hLbaR-CRkSq7_9Oh32YbVuAApAobubOhr3xtd3M1s_LSHuTiVzYCqswiffuhkjhkiNKNj6tQYEAyqwx3y5vg59zDLVGvhG","eFSsj7CGLik:APA91bFnOjS61WsWqWnl5HfDDPKxX2-I-SKYuTiLk7J-dJ6_zBnz9mrUPTSSZT9-t9KmA37xcAB-xpEXPVDcy664FhBOfbLwSl0YJ4Ujs8V661mxcsj4t4PpQSiPWQNHs5GAQkLYD7Y1","fFe26cp7-yI:APA91bFXQJUtQ7IEtFk6eQqy-4BNYgwwCCdXpZly9PsEIYl73v7zDVO28foahYU2bm1iHYpBjQAiUQ9pKxrqip7hRArozhZubP0K2Rmx182AnsuZ0VEX7w2-DmILI0IyK31ZC0YKZrzq","fHFe-mt-XOk:APA91bGN9GzpRywEWMmvJ5ey-xwB8foGqchQMWAn69zcYqAuqaVbwB20pIsNZYhbcOubpVUgchXXlVodvnA-U2r2fTApEML402pSArb5qX86rykB52mAuMR37nLCjUHO6jo1HldHdaEO","fcky58cBbo0:APA91bFELDfa9e0bgzXNtYG3WyjiitP6PIGvGOfmQDplgiQX3OayTF5rCjXvvQqTWDB0rzUN35MdMYGICxcmVn9B9vlFGpce9lpBwCLgEkza1vGThVANQ4Cu1HCRTFG6E4Et28xp2Sni","fs5Jpt8eI8Q:APA91bH-2Z8SXmoQRXFJ2OZRzZhznyxZ8IXVXtOR8rJCGpycWJMIWWMX1QTE8yTJiWJTGoyML8j0eGIHP0czhvHyLgi4u_aUePed3PGpA4KbmZ9e_-s-1JN4T1HFqfMe1uxyD4NxXlWG","fs_TplPeunc:APA91bHVpYV7CkmGo8cx4z4cwE-8HYAXUt-NjXecmuov435Nm-RUu0HWk7uyUT0eDNtu7KKKLnnM2QDbJl4nxinRs_lf11w18JXOCR9LMqovRTOC0SoMIZeFtNaBpKrF7J6yzkZZgjjL")

    devices.foreach { deviceId =>
      val fut = pushService.pushDevice(deviceId, artifactVersion).onSuccess { r =>
        println(r)
        println(deviceId)
      }.onFailure { ex =>
        ex.printStackTrace()
      }

      Await.result(fut)
    }
//    val fut = pushService.pushDevice(deviceId, artifactVersion).onSuccess { r =>
//      println(Owned.extract(r.content).toString)
//      val Buf.Utf8(str) = r.content
//      println(str)
//      println("ok")
//    }.onFailure { ex =>
//      ex.printStackTrace()
//    }

//    Await.result(fut)
  }

}
