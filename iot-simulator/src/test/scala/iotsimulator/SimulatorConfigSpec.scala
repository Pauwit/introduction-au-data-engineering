package iotsimulator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

class SimulatorConfigSpec extends AnyFlatSpec with Matchers {

  "deviceCount" should "use the env value when present and fall back to 5 otherwise" in {
    SimulatorConfig.deviceCount(Map.empty) shouldEqual 5
    SimulatorConfig.deviceCount(Map("DEVICE_COUNT" -> "12")) shouldEqual 12
  }

  it should "fall back to 5 when the env value is zero or negative" in {
    SimulatorConfig.deviceCount(Map("DEVICE_COUNT" -> "0")) shouldEqual 5
    SimulatorConfig.deviceCount(Map("DEVICE_COUNT" -> "-3")) shouldEqual 5
  }

  "tickInterval" should "use the env value when present and fall back to 30 seconds otherwise" in {
    SimulatorConfig.tickInterval(Map.empty) shouldEqual 30.seconds
    SimulatorConfig.tickInterval(Map("TICK_INTERVAL_SECONDS" -> "5")) shouldEqual 5.seconds
  }

  it should "fall back to 30 seconds when the env value is zero or negative" in {
    SimulatorConfig.tickInterval(Map("TICK_INTERVAL_SECONDS" -> "0")) shouldEqual 30.seconds
    SimulatorConfig.tickInterval(Map("TICK_INTERVAL_SECONDS" -> "-1")) shouldEqual 30.seconds
  }

  "bootstrapServers" should "default to localhost:9092" in {
    SimulatorConfig.bootstrapServers(Map.empty) shouldEqual "localhost:9092"
  }

  "topic" should "default to drone-events" in {
    SimulatorConfig.topic(Map.empty) shouldEqual "drone-events"
  }
}
