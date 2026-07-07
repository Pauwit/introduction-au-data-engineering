package alertservice

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant

class AlertEnricherSpec extends AnyFlatSpec with Matchers {

  private val timestamp = Instant.parse("2026-07-03T10:15:00Z")

  "enrich" should "attach owner and contact when the geohash matches a known zone" in {
    val alert = Alert(timestamp, "drone-001", 57.64911, 10.40744, 950.0, 15.0, "high temperature and smoke")
    val contacts = Map("u4pru" -> Contact("Office National des Forets - Vosges", "+33 3 88 00 11 22"))

    val enriched = AlertEnricher.enrich(alert, contacts, geohashPrecision = 5)

    enriched.owner shouldEqual "Office National des Forets - Vosges"
    enriched.contact shouldEqual "+33 3 88 00 11 22"
  }

  it should "default owner and contact to unknown when the zone has no match" in {
    val alert = Alert(timestamp, "drone-002", 0.0, 0.0, 950.0, 15.0, "high temperature and smoke")
    val contacts = Map("u4pru" -> Contact("Office National des Forets - Vosges", "+33 3 88 00 11 22"))

    val enriched = AlertEnricher.enrich(alert, contacts, geohashPrecision = 5)

    enriched.owner shouldEqual "unknown"
    enriched.contact shouldEqual "unknown"
  }

  it should "preserve the original alert fields" in {
    val alert = Alert(timestamp, "drone-003", 43.5, 5.2, 950.0, 15.0, "high temperature and smoke")

    val enriched = AlertEnricher.enrich(alert, Map.empty, geohashPrecision = 5)

    enriched.timestamp shouldEqual timestamp
    enriched.deviceId shouldEqual "drone-003"
    enriched.latitude shouldEqual 43.5
    enriched.longitude shouldEqual 5.2
    enriched.co2 shouldEqual 950.0
    enriched.humidity shouldEqual 15.0
    enriched.reason shouldEqual "high temperature and smoke"
  }
}
