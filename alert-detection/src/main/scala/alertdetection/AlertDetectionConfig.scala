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

  def windowDurationSeconds(env: Map[String, String]): Int = {
    val seconds = env.getOrElse("WINDOW_DURATION_SECONDS", "60").toInt
    if (seconds > 0) seconds else 60
  }
}
