package alertdetection

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, from_json, to_timestamp}
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

object DroneEventParser {

  val schema: StructType = StructType(Seq(
    StructField("timestamp", StringType, nullable = false),
    StructField("device_id", StringType, nullable = false),
    StructField("latitude", DoubleType, nullable = false),
    StructField("longitude", DoubleType, nullable = false),
    StructField("temperature", DoubleType, nullable = false),
    StructField("humidity", DoubleType, nullable = false),
    StructField("co2", DoubleType, nullable = false),
    StructField("smoke", DoubleType, nullable = false)
  ))

  def parse(raw: DataFrame): DataFrame =
    raw
      .select(col("value").cast(StringType).as("value"))
      .select(from_json(col("value"), schema).as("event"))
      .select("event.*")
      .withColumn("timestamp", to_timestamp(col("timestamp")))
}
