package alertservice

import io.circe.Decoder

import java.time.Instant
import scala.util.Try

final case class Alert(
  timestamp: Instant,
  deviceId: String,
  latitude: Double,
  longitude: Double,
  reason: String
)

object Alert {
  private implicit val instantDecoder: Decoder[Instant] =
    Decoder.decodeString.emap(value => Try(Instant.parse(value)).toOption.toRight(s"invalid timestamp: $value"))

  implicit val decoder: Decoder[Alert] = Decoder.forProduct5(
    "timestamp", "device_id", "latitude", "longitude", "reason"
  )(Alert.apply)
}
