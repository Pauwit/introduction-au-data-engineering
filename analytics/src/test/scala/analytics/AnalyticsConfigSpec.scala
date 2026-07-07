package analytics

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AnalyticsConfigSpec extends AnyFlatSpec with Matchers {

  "dataLakeRoot" should "default to ../data-lake" in {
    AnalyticsConfig.dataLakeRoot(Map.empty) shouldEqual "../data-lake"
  }

  it should "use the env value when present" in {
    AnalyticsConfig.dataLakeRoot(Map("DATA_LAKE_ROOT" -> "/mnt/lake")) shouldEqual "/mnt/lake"
  }

  "bronzePath" should "default to ../data-lake/bronze/drone-events" in {
    AnalyticsConfig.bronzePath(Map.empty) shouldEqual "../data-lake/bronze/drone-events"
  }

  it should "use the custom data lake root when present" in {
    AnalyticsConfig.bronzePath(Map("DATA_LAKE_ROOT" -> "/mnt/lake")) shouldEqual "/mnt/lake/bronze/drone-events"
  }

  "silverPath" should "default to ../data-lake/silver/drone-events" in {
    AnalyticsConfig.silverPath(Map.empty) shouldEqual "../data-lake/silver/drone-events"
  }

  it should "use the custom data lake root when present" in {
    AnalyticsConfig.silverPath(Map("DATA_LAKE_ROOT" -> "/mnt/lake")) shouldEqual "/mnt/lake/silver/drone-events"
  }

  "goldPath" should "default to ../data-lake/gold" in {
    AnalyticsConfig.goldPath(Map.empty) shouldEqual "../data-lake/gold"
  }

  it should "use the custom data lake root when present" in {
    AnalyticsConfig.goldPath(Map("DATA_LAKE_ROOT" -> "/mnt/lake")) shouldEqual "/mnt/lake/gold"
  }

  "sparkMaster" should "default to local[*]" in {
    AnalyticsConfig.sparkMaster(Map.empty) shouldEqual "local[*]"
  }

  it should "use the env value when present" in {
    AnalyticsConfig.sparkMaster(Map("SPARK_MASTER" -> "spark://spark-master:7077")) shouldEqual "spark://spark-master:7077"
  }
}
