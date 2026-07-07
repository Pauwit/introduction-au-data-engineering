package analytics

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{avg, col, count, when}

object GoldAnalytics {

  private val temperatureThreshold = 50.0
  private val smokeThreshold = 50.0
  private val co2Threshold = 600.0
  private val humidityThreshold = 30.0

  private val isAnomaly =
    col("temperature") > temperatureThreshold &&
      col("smoke") > smokeThreshold &&
      col("co2") > co2Threshold &&
      col("humidity") < humidityThreshold

  def co2AndHumidityByReadingType(silver: DataFrame): DataFrame =
    silver
      .withColumn("reading_type", when(isAnomaly, "anomaly").otherwise("normal"))
      .groupBy(col("reading_type"))
      .agg(
        avg("temperature").as("avg_temperature"),
        avg("humidity").as("avg_humidity"),
        avg("co2").as("avg_co2"),
        avg("smoke").as("avg_smoke"),
        count("*").as("reading_count")
      )

  def avgTemperatureByZone(silver: DataFrame): DataFrame =
    silver
      .withColumn("geohash", GeohashUdf.encode(col("latitude"), col("longitude")))
      .groupBy(col("geohash"))
      .agg(avg("temperature").as("avg_temperature"))

  def topDevicesByAnomalies(silver: DataFrame): DataFrame =
    silver
      .filter(isAnomaly)
      .groupBy(col("device_id"))
      .agg(count("*").as("anomaly_count"))
      .orderBy(col("anomaly_count").desc)

  def avgReadingsByHourOfDay(silver: DataFrame): DataFrame =
    silver
      .groupBy(col("hour"))
      .agg(avg("temperature").as("avg_temperature"), count("*").as("reading_count"))
      .orderBy(col("hour"))
}
