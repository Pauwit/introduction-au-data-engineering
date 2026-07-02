package iotsimulator

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DeviceProfileSpec extends AnyFlatSpec with Matchers {

  "simulatedFleet" should "produce the requested number of devices with stable ids and coordinates" in {
    val fleet = DeviceProfile.simulatedFleet(3)
    fleet.map(_.deviceId) shouldEqual List("drone-001", "drone-002", "drone-003")
    DeviceProfile.simulatedFleet(3) shouldEqual fleet
  }
}
