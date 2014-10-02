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

class ClientSpec extends Specification {

  implicit val formats = DefaultFormats

  class OkHttpAdapter extends HttpAdapter {

    var lastRequest: Option[HttpRequest] = None

    override def doHttp(request: HttpRequest) = {
      lastRequest = Some(request)
      Future { Response(200, "Ok") }
    }

    def getRequest = lastRequest
  }

  class FiveHundredHttpAdapter extends HttpAdapter {

    override def doHttp(request: HttpRequest): Future[Response] = {
      Future {
        Response(500, "Internal Server Error")
      }
    }
  }

  class SlowHttpAdapter extends HttpAdapter {

    override def doHttp(request: HttpRequest) = {
      Future.failed(new AskTimeoutException("I timed out!"))
    }
  }


  // Sequential because it's less work to share the client instance
  sequential

  "Client" should {

    val adapter = new OkHttpAdapter()
    val client = new Client(
      apiKey = "apiKey",
      appKey = "appKey",
      httpAdapter = adapter
    )

    "handle get all timeboards" in {
      val res = Await.result(client.getAllTimeboards, Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash?api_key=apiKey&app_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle get event" in {
      val res = Await.result(client.getEvent(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/events/12345?api_key=apiKey&app_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle add event" in {
      val res = Await.result(client.addEvent(title = "poop", text = "fart"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/events?api_key=apiKey&app_key=appKey")
      val body = parse(adapter.getRequest.get.entity.asString)
      (body \ "title").extract[String] must beEqualTo("poop")
      (body \ "text").extract[String] must beEqualTo("fart")

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "handle add timeboard" in {
      val res = Await.result(client.addTimeboard("POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash?api_key=apiKey&app_key=appKey")
      adapter.getRequest must beSome.which(_.entity.asString == "POOP")

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "handle get timeboard" in {
      val res = Await.result(client.getTimeboard(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash/12345?api_key=apiKey&app_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.GET)
    }

    "handle delete timeboard" in {
      val res = Await.result(client.deleteTimeboard(12345), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash/12345?api_key=apiKey&app_key=appKey")

      adapter.getRequest must beSome.which(_.method == HttpMethods.DELETE)
    }

    "handle update timeboard" in {
      val res = Await.result(client.updateTimeboard(12345, "POOP"), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/dash/12345?api_key=apiKey&app_key=appKey")
      adapter.getRequest must beSome.which(_.entity.asString == "POOP")

      adapter.getRequest must beSome.which(_.method == HttpMethods.PUT)
    }

    "handle get events" in {
      val res = Await.result(client.getEvents(start = 12345, end = 12346), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/events")
      val body = adapter.getRequest.get.entity.asString
      body must contain("end=12346")
      body must contain("start=12345")
      body must contain("api_key=apiKey")
      body must contain("app_key=appKey")
    }

    "handle add metrics" in {
      val res = Await.result(client.addMetrics(
        series = Seq(
          Metric(
            name = "foo.bar.test",
            points = Seq((1412183578, 12.0), (1412183579, 123.0)),
            host = Some("poop.example.com"),
            tags = Some(Seq("tag1", "tag2:foo")),
            metricType = Some("gauge")
          ),
          Metric(
            name = "foo.bar.gorch",
            points = Seq((1412183580, 12.0), (1412183581, 123.0)),
            host = Some("poop2.example.com"),
            tags = Some(Seq("tag3", "tag3:foo")),
            metricType = Some("counter")
          )
        )
      ), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/series?api_key=apiKey&app_key=appKey")
      val body = parse(adapter.getRequest.get.entity.asString)
      val names = for {
        JObject(series) <- body
        JField("name", JString(name)) <- series
      } yield name

      names must have size(2)
      names must contain(be_==("foo.bar.test")).exactly(1)
      names must contain(be_==("foo.bar.gorch")).exactly(1)

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }

    "shutdown" in {
      client.shutdown
      1 must beEqualTo(1)
    }
  }

  "Client with custom HttpAdapter" should {

    "handle user-supplied actor system" in {
      val adapter = new HttpAdapter(actorSystem = Some(ActorSystem("keen-test")))
      val attempt = Try({
        val client = new Client(
          apiKey = "abc",
          appKey = "123",
          httpAdapter = adapter
        )
      })
      attempt must beSuccessfulTry
    }
  }

  "Client 500 failures" should {

    val adapter = new FiveHundredHttpAdapter()
    val client = new Client(
      apiKey = "abc",
      appKey = "123",
      httpAdapter = adapter
    )

    "handle 500" in {
      val res = Await.result(client.getAllTimeboards, Duration(5, "second"))

      res.statusCode must beEqualTo(500)
    }
  }

  "Client future failures" should {

    val adapter = new SlowHttpAdapter()
    val client = new Client(
      apiKey = "abc",
      appKey = "123",
      httpAdapter = adapter
    )

    "handle timeout" in {
      Await.result(client.getAllTimeboards, Duration(10, "second")) must throwA[AskTimeoutException]
    }
  }
}