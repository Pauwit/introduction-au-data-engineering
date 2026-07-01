package iotsimulator

import io.circe.{Decoder, Encoder}

import java.time.Instant
import scala.util.Try

final case class DroneEvent(
  timestamp: Instant,
  deviceId: String,
  latitude: Double,
  longitude: Double,
  temperature: Double,
  humidity: Double,
  co2: Double,
  smoke: Double
)

object DroneEvent {
  private implicit val instantEncoder: Encoder[Instant] =
    Encoder.encodeString.contramap(_.toString)

  private implicit val instantDecoder: Decoder[Instant] =
    Decoder.decodeString.emap(value => Try(Instant.parse(value)).toOption.toRight(s"invalid timestamp: $value"))

  implicit val encoder: Encoder[DroneEvent] = Encoder.forProduct8(
    "timestamp", "device_id", "latitude", "longitude", "temperature", "humidity", "co2", "smoke"
  )(event => (event.timestamp, event.deviceId, event.latitude, event.longitude, event.temperature, event.humidity, event.co2, event.smoke))

  implicit val decoder: Decoder[DroneEvent] = Decoder.forProduct8(
    "timestamp", "device_id", "latitude", "longitude", "temperature", "humidity", "co2", "smoke"
  )(DroneEvent.apply)
}
