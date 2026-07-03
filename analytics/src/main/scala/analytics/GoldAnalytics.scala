package analytics

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{avg, col, count, dayofweek, when}

object GoldAnalytics {

  private val temperatureThreshold = 50.0
  private val smokeThreshold = 50.0

  def anomaliesByWeekday(silver: DataFrame): DataFrame =
    silver
      .filter(col("temperature") > temperatureThreshold && col("smoke") > smokeThreshold)
      .withColumn("day_type", when(dayofweek(col("timestamp")).isin(1, 7), "weekend").otherwise("weekday"))
      .groupBy(col("day_type"))
      .agg(count("*").as("anomaly_count"))

  def avgTemperatureByZone(silver: DataFrame): DataFrame =
    silver
      .withColumn("geohash", GeohashUdf.encode(col("latitude"), col("longitude")))
      .groupBy(col("geohash"))
      .agg(avg("temperature").as("avg_temperature"))

  def topDevicesByAnomalies(silver: DataFrame): DataFrame =
    silver
      .filter(col("temperature") > temperatureThreshold && col("smoke") > smokeThreshold)
      .groupBy(col("device_id"))
      .agg(count("*").as("anomaly_count"))
      .orderBy(col("anomaly_count").desc)

  def avgReadingsByHourOfDay(silver: DataFrame): DataFrame =
    silver
      .groupBy(col("hour"))
      .agg(avg("temperature").as("avg_temperature"), count("*").as("reading_count"))
      .orderBy(col("hour"))
}
