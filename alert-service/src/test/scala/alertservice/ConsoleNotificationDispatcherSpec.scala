package alertservice

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant

class ConsoleNotificationDispatcherSpec extends AnyFlatSpec with Matchers {

  "format" should "include the device, reason, owner and contact" in {
    val alert = EnrichedAlert(
      Instant.parse("2026-07-03T10:15:00Z"), "drone-001", 43.5, 5.2, 60.0, 70.0, 950.0, 15.0, "high temperature and smoke",
      "Office National des Forets - Vosges", "+33 3 88 00 11 22"
    )

    val line = ConsoleNotificationDispatcher.format(alert)

    line should include("drone-001")
    line should include("high temperature and smoke")
    line should include("temperature=60")
    line should include("smoke=70")
    line should include("co2=950")
    line should include("humidity=15")
    line should include("Office National des Forets - Vosges")
    line should include("+33 3 88 00 11 22")
  }
}
