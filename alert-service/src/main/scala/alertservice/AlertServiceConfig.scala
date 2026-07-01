package alertservice

import scala.concurrent.duration._
import scala.util.Try

object AlertServiceConfig {

  def bootstrapServers(env: Map[String, String]): String =
    env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")

  def alertsTopic(env: Map[String, String]): String =
    env.getOrElse("ALERTS_TOPIC", "alerts")

  def contactsResource(env: Map[String, String]): String =
    env.getOrElse("CONTACTS_RESOURCE", "contacts-seed.json")

  def contactsRefreshInterval(env: Map[String, String]): FiniteDuration =
    Try(env.getOrElse("CONTACTS_REFRESH_SECONDS", "300").toInt).toOption.filter(_ > 0).getOrElse(300).seconds

  def geohashPrecision(env: Map[String, String]): Int =
    Try(env.getOrElse("GEOHASH_PRECISION", "5").toInt).toOption.filter(_ > 0).getOrElse(5)

  def httpHost(env: Map[String, String]): String =
    env.getOrElse("HTTP_HOST", "0.0.0.0")

  def httpPort(env: Map[String, String]): Int =
    Try(env.getOrElse("HTTP_PORT", "8080").toInt).toOption.filter(_ > 0).getOrElse(8080)
}
