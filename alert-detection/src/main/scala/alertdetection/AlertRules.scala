package alertdetection

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, first, lit, max, min, window}

object AlertRules {

  private val temperatureThreshold = 50.0
  private val smokeThreshold = 50.0
  private val co2Threshold = 600.0
  private val humidityThreshold = 30.0

  def detect(events: DataFrame, windowDurationSeconds: Int): DataFrame = {
    val windowDuration = s"$windowDurationSeconds seconds"

    events
      .withWatermark("timestamp", windowDuration)
      .groupBy(col("device_id"), window(col("timestamp"), windowDuration))
      .agg(
        max("temperature").as("max_temperature"),
        max("smoke").as("max_smoke"),
        max("co2").as("max_co2"),
        min("humidity").as("min_humidity"),
        first("latitude").as("latitude"),
        first("longitude").as("longitude"),
        max("timestamp").as("timestamp")
      )
      .filter(
        col("max_temperature") > temperatureThreshold &&
          col("max_smoke") > smokeThreshold &&
          col("max_co2") > co2Threshold &&
          col("min_humidity") < humidityThreshold
      )
      .select(
        col("timestamp"),
        col("device_id"),
        col("latitude"),
        col("longitude"),
        col("max_temperature").as("temperature"),
        col("max_smoke").as("smoke"),
        col("max_co2").as("co2"),
        col("min_humidity").as("humidity"),
        lit("high temperature, smoke and co2 with low humidity").as("reason")
      )
  }
}
