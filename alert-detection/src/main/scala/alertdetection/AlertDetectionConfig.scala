package alertdetection

object AlertDetectionConfig {

  def bootstrapServers(env: Map[String, String]): String =
    env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")

  def inputTopic(env: Map[String, String]): String =
    env.getOrElse("DRONE_EVENTS_TOPIC", "drone-events")

  def outputTopic(env: Map[String, String]): String =
    env.getOrElse("ALERTS_TOPIC", "alerts")

  def checkpointLocation(env: Map[String, String]): String =
    env.getOrElse("CHECKPOINT_LOCATION", "checkpoints/alert-detection")

  def sparkMaster(env: Map[String, String]): String =
    env.getOrElse("SPARK_MASTER", "local[*]")
}
