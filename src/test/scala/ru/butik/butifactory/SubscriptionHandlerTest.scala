package ru.butik.butifactory

import io.finch.{Application, Error, Input}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers}
import ru.butik.butifactory.SubscribeHandler.SubscribeRequest
import io.circe.generic.auto._
import io.finch.circe._

class SubscriptionHandlerTest extends FunSpec
  with Matchers
  with MockFactory {

  it("should save subscription id") {
    val datastore = mock[Datastore]
    (datastore.addSubscription _).expects("group.name", "123").returns(
      Subscription("group.name", "123")
    )
    (datastore.findArtifactByName _).expects("group.name").returns(
      Some(Artifact("group.name"))
    )

    val request = SubscribeRequest("group.name", "123")
    val input = Input.post("/subscribe")
      .withBody[Application.Json](request)

    val Some(response) = SubscribeHandler.subscribe(datastore)(input).awaitValueUnsafe()

    assert(response.bundleName === "group.name")
    assert(response.result === true)
  }

  it("should reject subscription if no artifact") {
    val datastore = mock[Datastore]
    (datastore.findArtifactByName _).expects("group.name").returns(None)

    val request = SubscribeRequest("group.name", "123")
    val input = Input.post("/subscribe")
      .withBody[Application.Json](request)

    a[Error.NotValid] shouldBe thrownBy(SubscribeHandler.subscribe(datastore)(input).awaitValueUnsafe())
  }

}
