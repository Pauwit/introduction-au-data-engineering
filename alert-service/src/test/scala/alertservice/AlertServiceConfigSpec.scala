package alertservice

import scala.concurrent.duration._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AlertServiceConfigSpec extends AnyFlatSpec with Matchers {

  "bootstrapServers" should "default to localhost:9092" in {
    AlertServiceConfig.bootstrapServers(Map.empty) shouldEqual "localhost:9092"
  }

  it should "use the env value when present" in {
    AlertServiceConfig.bootstrapServers(Map("KAFKA_BOOTSTRAP_SERVERS" -> "broker:9092")) shouldEqual "broker:9092"
  }

  "alertsTopic" should "default to alerts" in {
    AlertServiceConfig.alertsTopic(Map.empty) shouldEqual "alerts"
  }

  "contactsResource" should "default to contacts-seed.json" in {
    AlertServiceConfig.contactsResource(Map.empty) shouldEqual "contacts-seed.json"
  }

  "contactsRefreshInterval" should "default to 300 seconds" in {
    AlertServiceConfig.contactsRefreshInterval(Map.empty) shouldEqual (300.seconds)
  }

  it should "fall back to the default on an invalid value" in {
    AlertServiceConfig.contactsRefreshInterval(Map("CONTACTS_REFRESH_SECONDS" -> "not-a-number")) shouldEqual (300.seconds)
  }

  "geohashPrecision" should "default to 5" in {
    AlertServiceConfig.geohashPrecision(Map.empty) shouldEqual 5
  }

  "httpHost" should "default to 0.0.0.0" in {
    AlertServiceConfig.httpHost(Map.empty) shouldEqual "0.0.0.0"
  }

  "httpPort" should "default to 8080" in {
    AlertServiceConfig.httpPort(Map.empty) shouldEqual 8080
  }

  it should "fall back to the default on an invalid value" in {
    AlertServiceConfig.httpPort(Map("HTTP_PORT" -> "not-a-number")) shouldEqual 8080
  }
}
