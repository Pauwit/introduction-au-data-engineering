package alertdetection

import org.apache.spark.sql.types.TimestampType
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DroneEventParserSpec extends AnyFlatSpec with Matchers with SparkSessionTestWrapper {

  import spark.implicits._

  "parse" should "turn a raw kafka value column into typed drone event columns" in {
    val json = """{"timestamp":"2026-07-03T10:15:30Z","device_id":"drone-001","latitude":43.5,"longitude":5.2,"temperature":21.4,"humidity":40.0,"co2":410.0,"smoke":1.2}"""
    val raw = Seq(json).toDF("value")

    val parsed = DroneEventParser.parse(raw)
    val row = parsed.collect().head

    row.getAs[String]("device_id") shouldEqual "drone-001"
    row.getAs[Double]("latitude") shouldEqual 43.5
    row.getAs[Double]("temperature") shouldEqual 21.4
    parsed.schema("timestamp").dataType shouldEqual TimestampType
  }
}
