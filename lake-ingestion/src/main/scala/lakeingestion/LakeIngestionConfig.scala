package lakeingestion

import scala.util.Try

object LakeIngestionConfig {

  def bootstrapServers(env: Map[String, String]): String =
    env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")

  def inputTopic(env: Map[String, String]): String =
    env.getOrElse("DRONE_EVENTS_TOPIC", "drone-events")

  def dataLakeRoot(env: Map[String, String]): String =
    env.getOrElse("DATA_LAKE_ROOT", "data-lake")

  def batchSize(env: Map[String, String]): Int =
    Try(env.getOrElse("BATCH_SIZE", "50").toInt).toOption.filter(_ > 0).getOrElse(50)

  def batchIntervalSeconds(env: Map[String, String]): Int =
    Try(env.getOrElse("BATCH_INTERVAL_SECONDS", "5").toInt).toOption.filter(_ > 0).getOrElse(5)
}
