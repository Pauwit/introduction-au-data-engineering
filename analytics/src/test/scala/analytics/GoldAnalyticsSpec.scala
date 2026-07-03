package analytics

import java.sql.Timestamp
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GoldAnalyticsSpec extends AnyFlatSpec with Matchers with SparkSessionTestWrapper {

  import spark.implicits._

  "anomaliesByWeekday" should "bucket an anomalous weekday row and an anomalous weekend row separately, excluding a calm row" in {
    val silver = Seq(
      (Timestamp.valueOf("2026-07-01 10:00:00"), "drone-001", 43.5, 5.2, 60.0, 20.0, 800.0, 60.0, "2026-07-01", 10),
      (Timestamp.valueOf("2026-07-04 10:00:00"), "drone-002", 43.5, 5.2, 70.0, 20.0, 800.0, 70.0, "2026-07-04", 10),
      (Timestamp.valueOf("2026-07-01 11:00:00"), "drone-003", 43.5, 5.2, 20.0, 40.0, 410.0, 1.0, "2026-07-01", 11)
    ).toDF("timestamp", "device_id", "latitude", "longitude", "temperature", "humidity", "co2", "smoke", "date", "hour")

    val gold = GoldAnalytics.anomaliesByWeekday(silver)
    val rows = gold.collect().map(row => row.getAs[String]("day_type") -> row.getAs[Long]("anomaly_count")).toMap

    rows("weekday") shouldEqual 1L
    rows("weekend") shouldEqual 1L
    rows.values.sum shouldEqual 2L
  }

  "avgTemperatureByZone" should "average temperatures for rows sharing the same geohash" in {
    val silver = Seq(
      (Timestamp.valueOf("2026-07-01 10:00:00"), "drone-001", 43.5, 5.2, 20.0, 40.0, 410.0, 1.0, "2026-07-01", 10),
      (Timestamp.valueOf("2026-07-01 11:00:00"), "drone-002", 43.5, 5.2, 30.0, 40.0, 410.0, 1.0, "2026-07-01", 11)
    ).toDF("timestamp", "device_id", "latitude", "longitude", "temperature", "humidity", "co2", "smoke", "date", "hour")

    val gold = GoldAnalytics.avgTemperatureByZone(silver)
    val rows = gold.collect()

    rows should have length 1
    rows.head.getAs[String]("geohash") shouldEqual Geohash.encode(43.5, 5.2, 5)
    rows.head.getAs[Double]("avg_temperature") shouldEqual 25.0
  }

  "topDevicesByAnomalies" should "order devices by descending anomaly count and exclude a non-anomalous device" in {
    val silver = Seq(
      (Timestamp.valueOf("2026-07-01 10:00:00"), "drone-001", 43.5, 5.2, 60.0, 20.0, 800.0, 60.0, "2026-07-01", 10),
      (Timestamp.valueOf("2026-07-01 11:00:00"), "drone-001", 43.5, 5.2, 65.0, 20.0, 800.0, 65.0, "2026-07-01", 11),
      (Timestamp.valueOf("2026-07-01 12:00:00"), "drone-002", 43.5, 5.2, 70.0, 20.0, 800.0, 70.0, "2026-07-01", 12),
      (Timestamp.valueOf("2026-07-01 13:00:00"), "drone-003", 43.5, 5.2, 20.0, 40.0, 410.0, 1.0, "2026-07-01", 13)
    ).toDF("timestamp", "device_id", "latitude", "longitude", "temperature", "humidity", "co2", "smoke", "date", "hour")

    val gold = GoldAnalytics.topDevicesByAnomalies(silver)
    val rows = gold.collect()

    rows should have length 2
    rows(0).getAs[String]("device_id") shouldEqual "drone-001"
    rows(0).getAs[Long]("anomaly_count") shouldEqual 2L
    rows(1).getAs[String]("device_id") shouldEqual "drone-002"
    rows(1).getAs[Long]("anomaly_count") shouldEqual 1L
    rows.map(_.getAs[String]("device_id")) should not contain "drone-003"
  }

  "avgReadingsByHourOfDay" should "average temperature and count readings per hour" in {
    val silver = Seq(
      (Timestamp.valueOf("2026-07-01 10:15:00"), "drone-001", 43.5, 5.2, 20.0, 40.0, 410.0, 1.0, "2026-07-01", 10),
      (Timestamp.valueOf("2026-07-01 10:45:00"), "drone-002", 43.5, 5.2, 30.0, 40.0, 410.0, 1.0, "2026-07-01", 10),
      (Timestamp.valueOf("2026-07-01 03:05:00"), "drone-003", 43.5, 5.2, 15.0, 40.0, 410.0, 1.0, "2026-07-01", 3)
    ).toDF("timestamp", "device_id", "latitude", "longitude", "temperature", "humidity", "co2", "smoke", "date", "hour")

    val gold = GoldAnalytics.avgReadingsByHourOfDay(silver)
    val rows = gold.collect()

    rows should have length 2
    rows(0).getAs[Int]("hour") shouldEqual 3
    rows(0).getAs[Double]("avg_temperature") shouldEqual 15.0
    rows(0).getAs[Long]("reading_count") shouldEqual 1L
    rows(1).getAs[Int]("hour") shouldEqual 10
    rows(1).getAs[Double]("avg_temperature") shouldEqual 25.0
    rows(1).getAs[Long]("reading_count") shouldEqual 2L
  }
}
