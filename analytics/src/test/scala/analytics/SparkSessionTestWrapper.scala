package analytics

import org.apache.spark.sql.SparkSession

trait SparkSessionTestWrapper {
  lazy val spark: SparkSession = SparkSession.builder()
    .master("local[2]")
    .appName("analytics-test")
    .config("spark.sql.shuffle.partitions", "2")
    .getOrCreate()
}
