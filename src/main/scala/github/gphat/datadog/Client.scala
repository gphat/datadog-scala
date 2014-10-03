package github.gphat.datadog

import grizzled.slf4j.Logging
import org.json4s._
import org.json4s.FieldSerializer._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read,write}
import scala.concurrent.Future
import java.nio.charset.StandardCharsets

class Client(
  scheme: String = "https",
  authority: String = "app.datadoghq.com",
  apiKey: String,
  appKey: String,
  httpAdapter: HttpAdapter = new HttpAdapter()) extends Logging {

  val metricSerializer = FieldSerializer[Metric](
    renameTo("metricType", "metric_type"),
    renameFrom("metric_type", "metricType")
  )

  implicit val formats = Serialization.formats(NoTypeHints) + metricSerializer

  def addComment(
    message: String, handle: Option[String] = None,
    relatedEventId: Option[Long] = None
  ): Future[Response] = {

    val json =
      ("message" -> message) ~
      ("handle" -> handle) ~
      ("related_event_id" -> relatedEventId)

    val path = Seq("comments").mkString("/")
    doRequest(path = path, method = "POST", body = Some(write(json)))
  }

  def addEvent(
    title: String, text: String, dateHappened: Option[String] = None,
    priority: Option[String] = None, tags: Option[Seq[String]] = None,
    alertType: Option[String] = None, aggregationKey: Option[String] = None,
    sourceTypeName: Option[String] = None
  ): Future[Response] = {

    val json =
      ("title" -> title) ~
      ("text" -> text) ~
      ("date_happened" -> dateHappened) ~
      ("priority" -> priority) ~
      ("tags" -> tags) ~
      ("alert_type" -> alertType) ~
      ("aggregation_key" -> aggregationKey) ~
      ("source_type_name" -> sourceTypeName)

    val path = Seq("events").mkString("/")
    doRequest(path = path, method = "POST", body = Some(compact(render(json))))
  }

  def addMetrics(series: Seq[Metric]): Future[Response] = {

    val json = (
      "series" -> series
    )

    val path = Seq("series").mkString("/")
    doRequest(path = path, method = "POST", body = Some(write(json)))
  }

  def addScreenboard(board: String): Future[Response] = {

    val path = Seq("screen").mkString("/")
    doRequest(path = path, method = "POST", body = Some(board))
  }

  // XXX source
  def addTags(hostId: String, tags: Seq[String]): Future[Response] = {

    val json = ("tags" -> tags)

    val path = Seq("tags", "hosts", hostId).mkString("/")
    doRequest(path = path, method = "POST", body = Some(write(json)))
  }

  def addAlert(alert: String): Future[Response] = {

    val path = Seq("alert").mkString("/")
    doRequest(path = path, method = "POST", body = Some(alert))
  }

  def addTimeboard(board: String): Future[Response] = {

    val path = Seq("dash").mkString("/")
    doRequest(path = path, method = "POST", body = Some(board))
  }

  def deleteAlert(alertId: Long): Future[Response] = {

    val path = Seq("alert", alertId).mkString("/")
    doRequest(path = path, method = "DELETE")
  }

  def deleteComment(commentId: Long): Future[Response] = {

    val path = Seq("comments", commentId).mkString("/")
    doRequest(path = path, method = "DELETE")
  }

  def deleteScreenboard(boardId: Long): Future[Response] = {

    val path = Seq("screen", boardId).mkString("/")
    doRequest(path = path, method = "DELETE")
  }

  def deleteTags(hostId: String): Future[Response] = {

    val path = Seq("tags", "hosts", hostId).mkString("/")
    doRequest(path = path, method = "DELETE")
  }

  def deleteTimeboard(boardId: Long): Future[Response] = {

    val path = Seq("dash", boardId).mkString("/")
    doRequest(path = path, method = "DELETE")
  }

  def getAlert(id: Long): Future[Response] = {
    val path = Seq("alert", id.toString).mkString("/")
    doRequest(path = path, method = "GET")
  }

  def getScreenboard(id: Long): Future[Response] = {
    val path = Seq("screen", id.toString).mkString("/")
    doRequest(path = path, method = "GET")
  }

  def getTimeboard(id: Long): Future[Response] = {
    val path = Seq("dash", id.toString).mkString("/")
    doRequest(path = path, method = "GET")
  }

  def getAllAlerts(): Future[Response] = {
    val path = Seq("alert").mkString("/")
    doRequest(path = path, method = "GET")
  }

  def getAllScreenboards(): Future[Response] = {
    val path = Seq("screen").mkString("/")
    doRequest(path = path, method = "GET")
  }

  def getAllTags: Future[Response] = {
    val path = Seq("tags", "hosts").mkString("/")
    doRequest(path = path, method = "GET")
  }

  def getAllTimeboards(): Future[Response] = {
    val path = Seq("dash").mkString("/")
    doRequest(path = path, method = "GET")
  }

  def getEvent(eventId: Long): Future[Response] = {
    val path = Seq("events", eventId.toString).mkString("/")
    doRequest(path = path, method = "GET")
  }

  def getEvents(
    start: Long, end: Long, priority: Option[String] = None,
    sources: Option[Seq[String]] = None, tags: Option[Seq[String]] = None
  ): Future[Response] = {

    val params = Map(
      "start" -> Some(start.toString),
      "end" -> Some(end.toString),
      "priority" -> priority,
      "sources" -> sources.map({ esses => esses.mkString(",") }),
      "tags" -> tags.map({ tees => tees.mkString(",") })
    )

    val path = Seq("events").mkString("/")
    doRequest(path = path, method = "GET", params = params, contentType = "form")
  }

  // XXX source and by_source?
  def getTags(hostId: String): Future[Response] = {
    val path = Seq("tags", "hosts", hostId).mkString("/")
    doRequest(path = path, method = "GET")
  }

  def graphSnapshot(query: String, start: Long, end: Long, eventQuery: Option[String]): Future[Response] = {
    val path = Seq("graph", "snapshot").mkString("/")
    doRequest(path = path, method = "GET", params = Map(
      "metric_query" -> Some(query),
      "start" -> Some(start.toString),
      "end" -> Some(end.toString),
      "event_query" -> eventQuery
    ))
  }

  def inviteUsers(emails: Seq[String]): Future[Response] = {

    val json = ("emails" -> emails)

    val path = Seq("invite_users").mkString("/")
    doRequest(path = path, method = "POST", body = Some(write(json)))
  }

  def muteAllAlerts: Future[Response] = {

    val path = Seq("mute_alerts").mkString("/")
    doRequest(path = path, method = "POST")
  }

  def search(query: String): Future[Response] = {
    val path = Seq("search").mkString("/")
    doRequest(path = path, method = "GET", params = Map("q" -> Some(query)))
  }

  def unmuteAllAlerts: Future[Response] = {

    val path = Seq("unmute_alerts").mkString("/")
    doRequest(path = path, method = "POST")
  }

  def updateAlert(alertId: Long, alert: String): Future[Response] = {

    val path = Seq("alert", alertId).mkString("/")
    doRequest(path = path, method = "PUT", body = Some(alert))
  }

  def updateComment(
    commentId: Long, message: Option[String], handle: Option[String] = None,
    relatedEventId: Option[Long] = None
  ): Future[Response] = {

    val json =
      ("message" -> message) ~
      ("handle" -> handle)

    val path = Seq("comments", commentId).mkString("/")
    doRequest(path = path, method = "PUT", body = Some(write(json)))
  }

  def updateScreenboard(boardId: Long, board: String): Future[Response] = {

    val path = Seq("screen", boardId).mkString("/")
    doRequest(path = path, method = "PUT", body = Some(board))
  }

  def updateTimeboard(boardId: Long, board: String): Future[Response] = {

    val path = Seq("dash", boardId).mkString("/")
    doRequest(path = path, method = "PUT", body = Some(board))
  }

  private def doRequest(
    path: String,
    method: String,
    body: Option[String] = None,
    params: Map[String,Option[String]] = Map.empty,
    contentType: String = "json") = {

    httpAdapter.doRequest(
      method = method, scheme = scheme, authority = authority, path = path,
      body = body, params = params ++ Map("api_key" -> Some(apiKey), "application_key" -> Some(appKey)),
      contentType = contentType
    )
  }

  /**
   * Disconnects any remaining connections. Both idle and active.
   */
  def shutdown {
    httpAdapter.shutdown
  }
}
