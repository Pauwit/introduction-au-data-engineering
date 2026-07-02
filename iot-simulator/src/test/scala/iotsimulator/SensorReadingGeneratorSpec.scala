package iotsimulator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant
import scala.util.Random

class SensorReadingGeneratorSpec extends AnyFlatSpec with Matchers {

  private val profile = DeviceProfile("drone-001", 43.5, 5.2)
  private val timestamp = Instant.parse("2026-07-03T10:00:00Z")

  "readingFor" should "stay in calm ranges when there is no fire event" in {
    val reading = SensorReadingGenerator.readingFor(profile, timestamp, isFireEvent = false, new Random(7))
    reading.deviceId shouldEqual "drone-001"
    reading.latitude shouldEqual 43.5
    reading.longitude shouldEqual 5.2
    reading.temperature should be < 30.0
    reading.co2 should be < 450.0
  }

  it should "produce a fire-like spike when a fire event is forced" in {
    val reading = SensorReadingGenerator.readingFor(profile, timestamp, isFireEvent = true, new Random(7))
    reading.temperature should be > 50.0
    reading.co2 should be > 850.0
    reading.smoke should be > 50.0
  }
}
