package alertdetection

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AlertDetectionConfigSpec extends AnyFlatSpec with Matchers {

  "bootstrapServers" should "default to localhost:9092" in {
    AlertDetectionConfig.bootstrapServers(Map.empty) shouldEqual "localhost:9092"
  }

  it should "use the env value when present" in {
    AlertDetectionConfig.bootstrapServers(Map("KAFKA_BOOTSTRAP_SERVERS" -> "broker:9092")) shouldEqual "broker:9092"
  }

  "inputTopic" should "default to drone-events" in {
    AlertDetectionConfig.inputTopic(Map.empty) shouldEqual "drone-events"
  }

  "outputTopic" should "default to alerts" in {
    AlertDetectionConfig.outputTopic(Map.empty) shouldEqual "alerts"
  }

  "checkpointLocation" should "default to checkpoints/alert-detection" in {
    AlertDetectionConfig.checkpointLocation(Map.empty) shouldEqual "checkpoints/alert-detection"
  }

  "sparkMaster" should "default to local[*]" in {
    AlertDetectionConfig.sparkMaster(Map.empty) shouldEqual "local[*]"
  }

  "windowDurationSeconds" should "default to 60" in {
    AlertDetectionConfig.windowDurationSeconds(Map.empty) shouldEqual 60
  }

  it should "use the env value when present" in {
    AlertDetectionConfig.windowDurationSeconds(Map("WINDOW_DURATION_SECONDS" -> "30")) shouldEqual 30
  }

  it should "fall back to the default on a non-positive value" in {
    AlertDetectionConfig.windowDurationSeconds(Map("WINDOW_DURATION_SECONDS" -> "-10")) shouldEqual 60
  }
}
