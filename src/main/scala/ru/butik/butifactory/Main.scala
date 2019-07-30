package ru.butik.butifactory

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.param.Stats
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import org.flywaydb.core.Flyway
import ru.butik.butifactory.ArtifactStorageBackend.{DiskArtifactStorageBackend, SelfHostedHTTPArtifactStorageFrontend}
import pureconfig.generic.auto._

object Main extends TwitterServer {
  val cfg: Config = pureconfig.loadConfigFromFilesOrThrow[Config](files = List(Paths.get("butifactory.conf")))

  val flyway: Flyway = Flyway.configure().dataSource(cfg.db, null, null).load
  flyway.migrate

  val db = DatastoreDoobie.init(cfg.db)

  val storageBackend = new DiskArtifactStorageBackend(cfg.dataDir)
  val storageFrontend = new SelfHostedHTTPArtifactStorageFrontend(cfg.servePath, cfg.dataDir)

  val pushService = new PushService(cfg.pushHost, cfg.pushPort, cfg.pushPath, cfg.pushKey)
  val apkService = new ApkService(db, storageBackend, storageFrontend, pushService)

  val api: Service[Request, Response] = ApiEndpoints.makeService(db, storageFrontend, apkService)

  def main(): Unit = {
    import com.twitter.conversions.storage._

    val server = Http.server
      .configured(Stats(statsReceiver))
      .withMaxRequestSize(50.megabyte)
      .serve(cfg.addr, api)

    onExit {
      val _ = Await.ready(server.close(), com.twitter.util.Duration(30, TimeUnit.SECONDS))
    }

    val _ = Await.ready(adminHttpServer)
  }
}
