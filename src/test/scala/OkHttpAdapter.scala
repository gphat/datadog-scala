package test

import akka.actor.ActorSystem
import akka.pattern.AskTimeoutException
import github.gphat.datadog._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future}
import spray.http._

class OkHttpAdapter extends HttpAdapter {

  var lastRequest: Option[HttpRequest] = None

  override def doHttp(request: HttpRequest) = {
    lastRequest = Some(request)
    Future { Response(200, "Ok") }
  }

  def getRequest = lastRequest
}
