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

class UserSpec extends Specification {

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

    "handle invite users" in {
      val res = Await.result(client.inviteUsers(Seq("friend@example.com")), Duration(5, "second"))

      res.statusCode must beEqualTo(200)
      adapter.getRequest must beSome.which(_.uri.toString == "https://app.datadoghq.com/api/v1/invite_users?api_key=apiKey&application_key=appKey")
      adapter.getRequest must beSome.which(_.method == HttpMethods.POST)

      val body = parse(adapter.getRequest.get.entity.asString)
      (body \ "emails")(0).extract[String] must beEqualTo("friend@example.com")
    }
  }
}