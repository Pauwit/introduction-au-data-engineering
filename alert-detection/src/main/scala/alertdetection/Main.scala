package alertdetection

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, struct, to_json}

object Main {

  def main(args: Array[String]): Unit = {
    val env = sys.env

    val spark = SparkSession.builder()
      .appName("alert-detection")
      .master(AlertDetectionConfig.sparkMaster(env))
      .config("spark.hadoop.fs.file.impl", "org.apache.hadoop.fs.RawLocalFileSystem")
      .getOrCreate()

    val rawEvents = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", AlertDetectionConfig.bootstrapServers(env))
      .option("subscribe", AlertDetectionConfig.inputTopic(env))
      .option("startingOffsets", "latest")
      .load()

    val events = DroneEventParser.parse(rawEvents)
    val alerts = AlertRules.detect(events, AlertDetectionConfig.windowDurationSeconds(env))

    val alertRecords = alerts.select(
      to_json(struct(col("timestamp"), col("device_id"), col("latitude"), col("longitude"), col("co2"), col("humidity"), col("reason"))).as("value")
    )

    val query = alertRecords.writeStream
      .format("kafka")
      .option("kafka.bootstrap.servers", AlertDetectionConfig.bootstrapServers(env))
      .option("topic", AlertDetectionConfig.outputTopic(env))
      .option("checkpointLocation", AlertDetectionConfig.checkpointLocation(env))
      .outputMode("append")
      .start()

    query.awaitTermination()
  }
}
