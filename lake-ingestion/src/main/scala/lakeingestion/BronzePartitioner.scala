package lakeingestion

import io.circe.Decoder
import io.circe.parser.decode

import java.time.{Instant, ZoneOffset}
import java.time.format.DateTimeFormatter

final case class RawTimestamp(timestamp: Instant)

object RawTimestamp {
  implicit val decoder: Decoder[RawTimestamp] = Decoder.forProduct1("timestamp")(RawTimestamp.apply)
}

object BronzePartitioner {

  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC)

  def partitionPath(root: String, timestamp: Instant): String = {
    val date = dateFormatter.format(timestamp)
    val hour = timestamp.atZone(ZoneOffset.UTC).getHour
    val hourLabel = "%02d".format(hour)
    s"$root/bronze/drone-events/date=$date/hour=$hourLabel"
  }

  def partitionFor(root: String, rawJson: String): Option[String] =
    decode[RawTimestamp](rawJson).toOption.map(raw => partitionPath(root, raw.timestamp))
}
