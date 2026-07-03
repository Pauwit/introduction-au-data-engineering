package analytics

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.{col, to_timestamp}
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

object BronzeEventParser {

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

  def read(spark: SparkSession, path: String): DataFrame =
    spark.read.schema(schema).json(path).withColumn("timestamp", to_timestamp(col("timestamp")))
}
