package alertdetection

import org.apache.spark.sql.SparkSession

trait SparkSessionTestWrapper {
  lazy val spark: SparkSession = SparkSession.builder()
    .master("local[2]")
    .appName("alert-detection-test")
    .config("spark.sql.shuffle.partitions", "2")
    .getOrCreate()
}
