package iotsimulator

import scala.util.Random

final case class DeviceProfile(deviceId: String, latitude: Double, longitude: Double)

object DeviceProfile {

  def simulatedFleet(count: Int): List[DeviceProfile] =
    (1 to count).toList.map { n =>
      val rng = new Random(n)
      DeviceProfile(
        deviceId = f"drone-$n%03d",
        latitude = 43.0 + rng.nextDouble() * 2.0,
        longitude = 5.0 + rng.nextDouble() * 2.0
      )
    }
}
