package iotsimulator

import scala.concurrent.duration._
import scala.util.Try

object SimulatorConfig {

  def bootstrapServers(env: Map[String, String]): String =
    env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")

  def topic(env: Map[String, String]): String =
    env.getOrElse("DRONE_EVENTS_TOPIC", "drone-events")

  def deviceCount(env: Map[String, String]): Int =
    Try(env.getOrElse("DEVICE_COUNT", "5").toInt).getOrElse(5)

  def tickInterval(env: Map[String, String]): FiniteDuration =
    Try(env.getOrElse("TICK_INTERVAL_SECONDS", "30").toInt).getOrElse(30).seconds
}
