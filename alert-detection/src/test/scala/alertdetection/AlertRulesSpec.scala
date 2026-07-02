package alertdetection

import java.sql.Timestamp
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AlertRulesSpec extends AnyFlatSpec with Matchers with SparkSessionTestWrapper {

  import spark.implicits._

  "detect" should "raise an alert for a device with high temperature and smoke" in {
    val events = Seq(
      (Timestamp.valueOf("2026-07-03 10:15:00"), "drone-001", 43.5, 5.2, 60.0, 15.0, 950.0, 70.0)
    ).toDF("timestamp", "device_id", "latitude", "longitude", "temperature", "humidity", "co2", "smoke")

    val alerts = AlertRules.detect(events).collect()

    alerts should have length 1
    alerts.head.getAs[String]("device_id") shouldEqual "drone-001"
    alerts.head.getAs[String]("reason") shouldEqual "high temperature and smoke"
  }

  it should "not raise an alert for a device with calm readings" in {
    val events = Seq(
      (Timestamp.valueOf("2026-07-03 10:15:00"), "drone-002", 43.6, 5.3, 21.0, 40.0, 410.0, 1.0)
    ).toDF("timestamp", "device_id", "latitude", "longitude", "temperature", "humidity", "co2", "smoke")

    val alerts = AlertRules.detect(events).collect()

    alerts should have length 0
  }
}
