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