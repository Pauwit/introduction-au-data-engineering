package analytics

import org.apache.spark.sql.SparkSession

object Main {

  def main(args: Array[String]): Unit = {
    val env = sys.env

    val spark = SparkSession.builder()
      .appName("analytics")
      .master(AnalyticsConfig.sparkMaster(env))
      .config("spark.sql.session.timeZone", "UTC")
      .getOrCreate()

    val bronze = BronzeEventParser.read(spark, AnalyticsConfig.bronzePath(env))
    val silver = SilverTransformer.clean(bronze)

    silver.write.mode("overwrite").partitionBy("date", "hour").parquet(AnalyticsConfig.silverPath(env))

    val silverForGold = spark.read.parquet(AnalyticsConfig.silverPath(env))

    val co2HumidityByReadingType = GoldAnalytics.co2AndHumidityByReadingType(silverForGold)
    val avgTemperatureByZone = GoldAnalytics.avgTemperatureByZone(silverForGold)
    val topDevicesByAnomalies = GoldAnalytics.topDevicesByAnomalies(silverForGold)
    val avgReadingsByHourOfDay = GoldAnalytics.avgReadingsByHourOfDay(silverForGold)

    co2HumidityByReadingType.write.mode("overwrite").parquet(s"${AnalyticsConfig.goldPath(env)}/co2_humidity_by_reading_type")
    avgTemperatureByZone.write.mode("overwrite").parquet(s"${AnalyticsConfig.goldPath(env)}/avg_temperature_by_zone")
    topDevicesByAnomalies.write.mode("overwrite").parquet(s"${AnalyticsConfig.goldPath(env)}/top_devices_by_anomalies")
    avgReadingsByHourOfDay.write.mode("overwrite").parquet(s"${AnalyticsConfig.goldPath(env)}/avg_readings_by_hour_of_day")

    println("average readings by reading type:")
    co2HumidityByReadingType.collect().foreach(row => println(s"  ${row.getAs[String]("reading_type")}: temp=${row.getAs[Double]("avg_temperature")} humidity=${row.getAs[Double]("avg_humidity")} co2=${row.getAs[Double]("avg_co2")} smoke=${row.getAs[Double]("avg_smoke")} (${row.getAs[Long]("reading_count")} readings)"))

    println("average temperature by zone:")
    avgTemperatureByZone.collect().foreach(row => println(s"  ${row.getAs[String]("geohash")}: ${row.getAs[Double]("avg_temperature")}"))

    println("top devices by anomaly count:")
    topDevicesByAnomalies.collect().foreach(row => println(s"  ${row.getAs[String]("device_id")}: ${row.getAs[Long]("anomaly_count")}"))

    println("average temperature by hour of day:")
    avgReadingsByHourOfDay.collect().foreach(row => println(s"  ${row.getAs[Int]("hour")}h: ${row.getAs[Double]("avg_temperature")}"))

    val readingTypeList = co2HumidityByReadingType.collect().map { r =>
      s"""{"reading_type": "${r.getAs[String]("reading_type")}", "avg_temperature": ${math.round(r.getAs[Double]("avg_temperature") * 10.0) / 10.0}, "avg_humidity": ${math.round(r.getAs[Double]("avg_humidity") * 10.0) / 10.0}, "avg_co2": ${math.round(r.getAs[Double]("avg_co2") * 10.0) / 10.0}, "avg_smoke": ${math.round(r.getAs[Double]("avg_smoke") * 10.0) / 10.0}, "reading_count": ${r.getAs[Long]("reading_count")}}"""
    }.mkString("[", ", ", "]")

    val zoneList = avgTemperatureByZone.collect().map { r =>
      s"""{"geohash": "${r.getAs[String]("geohash")}", "avg_temperature": ${math.round(r.getAs[Double]("avg_temperature") * 10.0) / 10.0}}"""
    }.mkString("[", ", ", "]")

    val devicesList = topDevicesByAnomalies.collect().map { r =>
      s"""{"device_id": "${r.getAs[String]("device_id")}", "anomaly_count": ${r.getAs[Long]("anomaly_count")}}"""
    }.mkString("[", ", ", "]")

    val hourList = avgReadingsByHourOfDay.collect().map { r =>
      s"""{"hour": ${r.getAs[Int]("hour")}, "avg_temperature": ${math.round(r.getAs[Double]("avg_temperature") * 10.0) / 10.0}, "reading_count": ${r.getAs[Long]("reading_count")}}"""
    }.mkString("[", ", ", "]")

    val summaryJson =
      s"""{
         |  "co2_humidity_by_reading_type": $readingTypeList,
         |  "avg_temperature_by_zone": $zoneList,
         |  "top_devices_by_anomalies": $devicesList,
         |  "avg_readings_by_hour_of_day": $hourList
         |}""".stripMargin

    val summaryPath = java.nio.file.Paths.get(s"${AnalyticsConfig.goldPath(env)}/gold-summary.json")
    java.nio.file.Files.createDirectories(summaryPath.getParent)
    java.nio.file.Files.write(summaryPath, summaryJson.getBytes("UTF-8"))

    spark.stop()
  }
}
