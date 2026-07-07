package lakeingestion

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class LakeIngestionConfigSpec extends AnyFlatSpec with Matchers {

  "bootstrapServers" should "default to localhost:9092" in {
    LakeIngestionConfig.bootstrapServers(Map.empty) shouldEqual "localhost:9092"
  }

  it should "use the env value when present" in {
    LakeIngestionConfig.bootstrapServers(Map("KAFKA_BOOTSTRAP_SERVERS" -> "broker:9092")) shouldEqual "broker:9092"
  }

  "inputTopic" should "default to drone-events" in {
    LakeIngestionConfig.inputTopic(Map.empty) shouldEqual "drone-events"
  }

  it should "use the env value when present" in {
    LakeIngestionConfig.inputTopic(Map("DRONE_EVENTS_TOPIC" -> "custom-events")) shouldEqual "custom-events"
  }

  "dataLakeRoot" should "default to ../data-lake" in {
    LakeIngestionConfig.dataLakeRoot(Map.empty) shouldEqual "../data-lake"
  }

  it should "use the env value when present" in {
    LakeIngestionConfig.dataLakeRoot(Map("DATA_LAKE_ROOT" -> "/mnt/lake")) shouldEqual "/mnt/lake"
  }

  "batchSize" should "default to 50" in {
    LakeIngestionConfig.batchSize(Map.empty) shouldEqual 50
  }

  it should "use the env value when present" in {
    LakeIngestionConfig.batchSize(Map("BATCH_SIZE" -> "100")) shouldEqual 100
  }

  "batchIntervalSeconds" should "default to 5" in {
    LakeIngestionConfig.batchIntervalSeconds(Map.empty) shouldEqual 5
  }

  it should "use the env value when present" in {
    LakeIngestionConfig.batchIntervalSeconds(Map("BATCH_INTERVAL_SECONDS" -> "10")) shouldEqual 10
  }
}
