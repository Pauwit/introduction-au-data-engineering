package alertservice

import io.circe.parser.decode
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant

class AlertSpec extends AnyFlatSpec with Matchers {

  "Alert decoder" should "parse a valid alert payload" in {
    val json =
      """{"timestamp":"2026-07-03T10:15:00Z","device_id":"drone-001","latitude":43.5,"longitude":5.2,"temperature":60.0,"smoke":70.0,"co2":950.0,"humidity":15.0,"reason":"high temperature and smoke"}"""

    val result = decode[Alert](json)

    result shouldEqual Right(Alert(Instant.parse("2026-07-03T10:15:00Z"), "drone-001", 43.5, 5.2, 60.0, 70.0, 950.0, 15.0, "high temperature and smoke"))
  }

  it should "fail to parse an invalid timestamp" in {
    val json =
      """{"timestamp":"not-a-date","device_id":"drone-001","latitude":43.5,"longitude":5.2,"temperature":60.0,"smoke":70.0,"co2":950.0,"humidity":15.0,"reason":"high temperature and smoke"}"""

    decode[Alert](json).isLeft shouldEqual true
  }
}
