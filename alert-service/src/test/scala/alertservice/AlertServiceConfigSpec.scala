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

  "contactsResource" should "default to contacts/contacts-seed.json" in {
    AlertServiceConfig.contactsResource(Map.empty) shouldEqual "contacts/contacts-seed.json"
  }

  "dataLakeRoot" should "default to ../data-lake" in {
    AlertServiceConfig.dataLakeRoot(Map.empty) shouldEqual "../data-lake"
  }

  "contactsPath" should "join the data lake root and the contacts resource" in {
    AlertServiceConfig.contactsPath(Map("DATA_LAKE_ROOT" -> "/lake", "CONTACTS_RESOURCE" -> "contacts/seed.json")) shouldEqual "/lake/contacts/seed.json"
  }

  "contactsRefreshInterval" should "default to 300 seconds" in {
    AlertServiceConfig.contactsRefreshInterval(Map.empty) shouldEqual (300.seconds)
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
}
