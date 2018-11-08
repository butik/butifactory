package ru.butik.butifactory

import com.twitter.finagle.Http
import com.twitter.util.Await
import org.flywaydb.core.Flyway
import ru.butik.butifactory.ArtifactStorageBackend.{DiskArtifactStorageBackend, SelfHostedHTTPArtifactStorageFrontend}
import io.finch.circe._

object Main extends App {
  val flyway: Flyway = Flyway.configure().dataSource("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", null, null).load
  flyway.migrate

  val db = DatastoreDoobie.init()

  val storageBackend = new DiskArtifactStorageBackend("target/tmp")
  val storageFrontend = new SelfHostedHTTPArtifactStorageFrontend("artifact.butik.ru")
  val storage = new ArtifactStorageImpl(storageBackend, storageFrontend)

  val apkService = new ApkService(db, storageBackend)

  Await.ready(
    Http.server.serve(
      addr = ":8081",
      Endpoint.makeService(apkService)))
}
