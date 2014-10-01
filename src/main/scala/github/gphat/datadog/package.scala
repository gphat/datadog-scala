package github.gphat

package object datadog {

  case class Metric(
    name: String, points: Seq[(Long,Double)], metricType: Option[String] = None,
    tags: Option[Seq[String]] = None, host: Option[String]
  )

  case class Response(statusCode: Int, body: String)
}