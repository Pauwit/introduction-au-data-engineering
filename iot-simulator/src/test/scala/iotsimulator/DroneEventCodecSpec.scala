package iotsimulator

import io.circe.parser.decode
import io.circe.syntax._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant

class DroneEventCodecSpec extends AnyFlatSpec with Matchers {

  "DroneEvent" should "round-trip through json with snake_case keys" in {
    val event = DroneEvent(
      timestamp = Instant.parse("2026-07-03T10:15:30Z"),
      deviceId = "drone-001",
      latitude = 43.5,
      longitude = 5.2,
      temperature = 21.4,
      humidity = 40.0,
      co2 = 410.0,
      smoke = 1.2
    )

    val json = event.asJson.noSpaces

    json should include("\"device_id\"")
    decode[DroneEvent](json) shouldEqual Right(event)
  }
}
