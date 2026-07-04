package alertdetection

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, first, lit, max, window}

object AlertRules {

  private val temperatureThreshold = 50.0
  private val smokeThreshold = 50.0

  def detect(events: DataFrame, windowDurationSeconds: Int): DataFrame = {
    val windowDuration = s"$windowDurationSeconds seconds"

    events
      .withWatermark("timestamp", windowDuration)
      .groupBy(col("device_id"), window(col("timestamp"), windowDuration))
      .agg(
        max("temperature").as("max_temperature"),
        max("smoke").as("max_smoke"),
        first("latitude").as("latitude"),
        first("longitude").as("longitude"),
        max("timestamp").as("timestamp")
      )
      .filter(col("max_temperature") > temperatureThreshold && col("max_smoke") > smokeThreshold)
      .select(
        col("timestamp"),
        col("device_id"),
        col("latitude"),
        col("longitude"),
        lit("high temperature and smoke").as("reason")
      )
  }
}
