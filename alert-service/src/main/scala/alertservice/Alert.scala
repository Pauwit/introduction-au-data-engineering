package alertservice

import io.circe.Decoder

import java.time.Instant

final case class Alert(
  timestamp: Instant,
  deviceId: String,
  latitude: Double,
  longitude: Double,
  co2: Double,
  humidity: Double,
  reason: String
)

object Alert {
  implicit val decoder: Decoder[Alert] = Decoder.forProduct7(
    "timestamp", "device_id", "latitude", "longitude", "co2", "humidity", "reason"
  )(Alert.apply)
}
