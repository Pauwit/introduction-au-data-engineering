package analytics

import org.apache.spark.sql.types.TimestampType
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BronzeEventParserSpec extends AnyFlatSpec with Matchers with SparkSessionTestWrapper {

  "read" should "parse bronze drone events into a typed schema" in {
    val df = BronzeEventParser.read(spark, "src/test/resources/bronze-sample")

    df.count() shouldEqual 2
    df.schema("timestamp").dataType shouldEqual TimestampType

    val rows = df.collect()

    rows(0).getAs[String]("device_id") shouldEqual "drone-001"
    rows(0).getAs[Double]("temperature") shouldEqual 21.4

    rows(1).getAs[String]("device_id") shouldEqual "drone-002"
    rows(1).getAs[Double]("temperature") shouldEqual 55.0
  }
}
