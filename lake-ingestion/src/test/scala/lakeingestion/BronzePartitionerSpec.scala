package lakeingestion

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.time.Instant

class BronzePartitionerSpec extends AnyFlatSpec with Matchers {

  "partitionPath" should "build the expected path string for a known timestamp" in {
    val timestamp = Instant.parse("2026-07-03T10:15:30Z")
    val path = BronzePartitioner.partitionPath("data-lake", timestamp)
    path shouldEqual "data-lake/bronze/drone-events/date=2026-07-03/hour=10"
  }

  it should "zero-pad the hour for single-digit hours" in {
    val timestamp = Instant.parse("2026-07-03T05:12:00Z")
    val path = BronzePartitioner.partitionPath("data-lake", timestamp)
    path shouldEqual "data-lake/bronze/drone-events/date=2026-07-03/hour=05"
  }

  "partitionFor" should "return Some(path) for a valid drone-event JSON payload" in {
    val json = """{"timestamp":"2026-07-03T10:15:30Z","device_id":"drone-001","latitude":43.5,"longitude":5.2,"temperature":21.4,"humidity":40.0,"co2":410.0,"smoke":1.2}"""
    val result = BronzePartitioner.partitionFor("data-lake", json)
    result shouldEqual Some("data-lake/bronze/drone-events/date=2026-07-03/hour=10")
  }

  it should "return None for malformed JSON" in {
    val json = """{"timestamp":"2026-07-03T10:15:30Z"invalid}"""
    val result = BronzePartitioner.partitionFor("data-lake", json)
    result shouldEqual None
  }

  it should "return None for JSON missing the timestamp field" in {
    val json = """{"device_id":"drone-001","latitude":43.5,"longitude":5.2,"temperature":21.4,"humidity":40.0,"co2":410.0,"smoke":1.2}"""
    val result = BronzePartitioner.partitionFor("data-lake", json)
    result shouldEqual None
  }
}
