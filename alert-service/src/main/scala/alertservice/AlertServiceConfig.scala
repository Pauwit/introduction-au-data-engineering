package alertservice

import scala.concurrent.duration._

object AlertServiceConfig {

  def bootstrapServers(env: Map[String, String]): String =
    env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")

  def alertsTopic(env: Map[String, String]): String =
    env.getOrElse("ALERTS_TOPIC", "alerts")

  def contactsResource(env: Map[String, String]): String =
    env.getOrElse("CONTACTS_RESOURCE", "contacts-seed.json")

  def contactsRefreshInterval(env: Map[String, String]): FiniteDuration =
    env.getOrElse("CONTACTS_REFRESH_SECONDS", "300").toInt.seconds

  def geohashPrecision(env: Map[String, String]): Int =
    env.getOrElse("GEOHASH_PRECISION", "5").toInt

  def httpHost(env: Map[String, String]): String =
    env.getOrElse("HTTP_HOST", "0.0.0.0")

  def httpPort(env: Map[String, String]): Int =
    env.getOrElse("HTTP_PORT", "8080").toInt
}
