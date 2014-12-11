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

class MetricSpec extends Specification {

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
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/series?api_key=apiKey&application_key=appKey")
      val body = parse(adapter.getRequest.get.entity.asString)
      val names = for {
        JObject(series) <- body
        JField("metric", JString(name)) <- series
      } yield name

      names must have size(2)
      names must contain(be_==("foo.bar.test")).exactly(1)
      names must contain(be_==("foo.bar.gorch")).exactly(1)

      val points = for {
        JObject(series) <- body
        JField("points", JArray(point)) <- series
      } yield point

      points must have size(2)
      points must contain(be_==(Seq(JArray(List(JInt(1412183578), JDouble(12.0))), JArray(List(JInt(1412183579), JDouble(123.0)))))).exactly(1)
      points must contain(be_==(Seq(JArray(List(JInt(1412183580), JDouble(12.0))), JArray(List(JInt(1412183581), JDouble(123.0)))))).exactly(1)

      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)
    }
  }
}
