package alertservice

import io.circe.Decoder

import java.time.Instant

final case class Alert(
  timestamp: Instant,
  deviceId: String,
  latitude: Double,
  longitude: Double,
  reason: String
)

object Alert {
  implicit val decoder: Decoder[Alert] = Decoder.forProduct5(
    "timestamp", "device_id", "latitude", "longitude", "reason"
  )(Alert.apply)
}
