package alertservice

import io.circe.parser.decode
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant

class AlertSpec extends AnyFlatSpec with Matchers {

  "Alert decoder" should "parse a valid alert payload" in {
    val json =
      """{"timestamp":"2026-07-03T10:15:00Z","device_id":"drone-001","latitude":43.5,"longitude":5.2,"reason":"high temperature and smoke"}"""

    val result = decode[Alert](json)

    result shouldEqual Right(Alert(Instant.parse("2026-07-03T10:15:00Z"), "drone-001", 43.5, 5.2, "high temperature and smoke"))
  }

  it should "fail to parse an invalid timestamp" in {
    val json =
      """{"timestamp":"not-a-date","device_id":"drone-001","latitude":43.5,"longitude":5.2,"reason":"high temperature and smoke"}"""

    decode[Alert](json).isLeft shouldEqual true
  }
}
