package analytics

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{col, date_format, hour}

object SilverTransformer {

  def clean(bronze: DataFrame): DataFrame =
    bronze
      .filter(col("latitude").between(-90.0, 90.0) && col("longitude").between(-180.0, 180.0))
      .filter(col("temperature").isNotNull && col("humidity").isNotNull && col("co2").isNotNull && col("smoke").isNotNull)
      .dropDuplicates("device_id", "timestamp")
      .withColumn("date", date_format(col("timestamp"), "yyyy-MM-dd"))
      .withColumn("hour", hour(col("timestamp")))
}
