package alertservice

import io.circe.Encoder

import java.time.Instant

final case class EnrichedAlert(
  timestamp: Instant,
  deviceId: String,
  latitude: Double,
  longitude: Double,
  temperature: Double,
  smoke: Double,
  co2: Double,
  humidity: Double,
  reason: String,
  owner: String,
  contact: String
)

object EnrichedAlert {
  private implicit val instantEncoder: Encoder[Instant] =
    Encoder.encodeString.contramap(_.toString)

  implicit val encoder: Encoder[EnrichedAlert] = Encoder.forProduct11(
    "timestamp", "device_id", "latitude", "longitude", "temperature", "smoke", "co2", "humidity", "reason", "owner", "contact"
  )(alert => (alert.timestamp, alert.deviceId, alert.latitude, alert.longitude, alert.temperature, alert.smoke, alert.co2, alert.humidity, alert.reason, alert.owner, alert.contact))
}
