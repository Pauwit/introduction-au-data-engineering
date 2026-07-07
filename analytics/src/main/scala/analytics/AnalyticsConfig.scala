package analytics

object AnalyticsConfig {

  def dataLakeRoot(env: Map[String, String]): String =
    env.getOrElse("DATA_LAKE_ROOT", "../data-lake")

  def bronzePath(env: Map[String, String]): String =
    s"${dataLakeRoot(env)}/bronze/drone-events"

  def silverPath(env: Map[String, String]): String =
    s"${dataLakeRoot(env)}/silver/drone-events"

  def goldPath(env: Map[String, String]): String =
    s"${dataLakeRoot(env)}/gold"

  def sparkMaster(env: Map[String, String]): String =
    env.getOrElse("SPARK_MASTER", "local[*]")
}
