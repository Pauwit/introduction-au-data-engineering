package analytics

import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.udf

object GeohashUdf {

  private val precision = 5

  private val encodeUdf = udf((latitude: Double, longitude: Double) => Geohash.encode(latitude, longitude, precision))

  def encode(latitude: Column, longitude: Column): Column = encodeUdf(latitude, longitude)
}
