package alertservice

import io.circe.Encoder

import java.time.Instant

final case class EnrichedAlert(
  timestamp: Instant,
  deviceId: String,
  latitude: Double,
  longitude: Double,
  reason: String,
  owner: String,
  contact: String
)

object EnrichedAlert {
  private implicit val instantEncoder: Encoder[Instant] =
    Encoder.encodeString.contramap(_.toString)

  implicit val encoder: Encoder[EnrichedAlert] = Encoder.forProduct7(
    "timestamp", "device_id", "latitude", "longitude", "reason", "owner", "contact"
  )(alert => (alert.timestamp, alert.deviceId, alert.latitude, alert.longitude, alert.reason, alert.owner, alert.contact))
}
