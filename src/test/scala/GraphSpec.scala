package test

import akka.actor.ActorSystem
import akka.pattern.AskTimeoutException
import github.gphat.datadog._
import java.nio.charset.StandardCharsets
import org.json4s._
import org.json4s.native.JsonMethods._
import org.specs2.mutable.Specification
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await,Future,Promise}
import scala.util.Try
import spray.http._

class GraphSpec extends Specification {

  implicit val formats = DefaultFormats

  // Sequential because it's less work to share the client instance
  sequential

  "Client" should {

    val adapter = new OkHttpAdapter()
    val client = new Client(
      apiKey = "apiKey",
      appKey = "appKey",
      httpAdapter = adapter
    )

    "handle graph snapshot" in {
      val res = Await.result(client.graphSnapshot(query = "q", start = 1234, end = 1235, eventQuery = Some("e")), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      val uri = adapter.getRequest.get.uri.toString
      uri must contain("https://app.datadoghq.com/api/v1/graph/snapshot")
      uri must contain("api_key=apiKey")
      uri must contain("application_key=appKey")
      uri must contain("metric_query=q")
      uri must contain("event_query=e")
      uri must contain("start=1234")
      uri must contain("end=1235")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }
  }
}