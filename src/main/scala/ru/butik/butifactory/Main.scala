package ru.butik.butifactory

import java.nio.file.Paths

import com.twitter.finagle.Http
import com.twitter.util.Await
import org.flywaydb.core.Flyway
import ru.butik.butifactory.ArtifactStorageBackend.{DiskArtifactStorageBackend, SelfHostedHTTPArtifactStorageFrontend}
import io.finch.circe._
import pureconfig.generic.auto._

object Main extends App {
  val cfg: Config = pureconfig.loadConfigFromFilesOrThrow[Config](files = List(Paths.get("butifactory.conf")))

  val flyway: Flyway = Flyway.configure().dataSource(cfg.db, null, null).load
  flyway.migrate

  val db = DatastoreDoobie.init(cfg.db)

  val storageBackend = new DiskArtifactStorageBackend(cfg.dataDir)
  val storageFrontend = new SelfHostedHTTPArtifactStorageFrontend(cfg.servePath)
  val storage = new ArtifactStorageImpl(storageBackend, storageFrontend)

  val apkService = new ApkService(db, storageBackend)

  Await.ready(
    Http.server.serve(
      addr = cfg.addr,
      Endpoint.makeService(db, storageFrontend, apkService)))
}
