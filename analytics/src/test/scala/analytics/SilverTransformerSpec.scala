package analytics

import java.sql.Timestamp
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SilverTransformerSpec extends AnyFlatSpec with Matchers with SparkSessionTestWrapper {

  import spark.implicits._

  "clean" should "collapse a duplicate (device_id, timestamp) pair into one row" in {
    val events = Seq(
      (Timestamp.valueOf("2026-07-03 10:15:00"), "drone-001", 43.5, 5.2, 21.0, 40.0, 410.0, 1.0),
      (Timestamp.valueOf("2026-07-03 10:15:00"), "drone-001", 43.5, 5.2, 21.0, 40.0, 410.0, 1.0)
    ).toDF("timestamp", "device_id", "latitude", "longitude", "temperature", "humidity", "co2", "smoke")

    val silver = SilverTransformer.clean(events)

    silver.count() shouldEqual 1
  }

  it should "drop a row with a latitude or longitude out of range" in {
    val events = Seq(
      (Timestamp.valueOf("2026-07-03 10:15:00"), "drone-002", 95.0, 5.2, 21.0, 40.0, 410.0, 1.0),
      (Timestamp.valueOf("2026-07-03 10:16:00"), "drone-003", 43.5, 190.0, 21.0, 40.0, 410.0, 1.0),
      (Timestamp.valueOf("2026-07-03 10:17:00"), "drone-004", 43.5, 5.2, 21.0, 40.0, 410.0, 1.0)
    ).toDF("timestamp", "device_id", "latitude", "longitude", "temperature", "humidity", "co2", "smoke")

    val silver = SilverTransformer.clean(events)
    val rows = silver.collect()

    rows should have length 1
    rows.head.getAs[String]("device_id") shouldEqual "drone-004"
  }

  it should "drop a row with a null measurement column" in {
    val events = Seq(
      (Timestamp.valueOf("2026-07-03 10:15:00"), "drone-005", 43.5, 5.2, Option.empty[Double], 40.0, 410.0, 1.0),
      (Timestamp.valueOf("2026-07-03 10:16:00"), "drone-006", 43.5, 5.2, Some(21.0), 40.0, 410.0, 1.0)
    ).toDF("timestamp", "device_id", "latitude", "longitude", "temperature", "humidity", "co2", "smoke")

    val silver = SilverTransformer.clean(events)
    val rows = silver.collect()

    rows should have length 1
    rows.head.getAs[String]("device_id") shouldEqual "drone-006"
  }

  it should "derive date and hour columns from the timestamp of a surviving row" in {
    val events = Seq(
      (Timestamp.valueOf("2026-07-03 14:37:00"), "drone-007", 43.5, 5.2, 21.0, 40.0, 410.0, 1.0)
    ).toDF("timestamp", "device_id", "latitude", "longitude", "temperature", "humidity", "co2", "smoke")

    val silver = SilverTransformer.clean(events)
    val rows = silver.collect()

    rows should have length 1
    rows.head.getAs[String]("date") shouldEqual "2026-07-03"
    rows.head.getAs[Int]("hour") shouldEqual 14
  }
}
