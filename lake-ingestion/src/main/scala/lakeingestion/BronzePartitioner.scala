package lakeingestion

import io.circe.Decoder
import io.circe.parser.decode

import java.time.{Instant, ZoneOffset}
import java.time.format.DateTimeFormatter
import scala.util.Try

final case class RawTimestamp(timestamp: Instant)

object RawTimestamp {
  private implicit val instantDecoder: Decoder[Instant] =
    Decoder.decodeString.emap(value => Try(Instant.parse(value)).toOption.toRight(s"invalid timestamp: $value"))

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
