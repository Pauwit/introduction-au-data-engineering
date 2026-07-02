package iotsimulator

import java.time.Instant
import scala.util.Random

object SensorReadingGenerator {

  private val fireProbability = 0.03

  def readingFor(profile: DeviceProfile, timestamp: Instant, isFireEvent: Boolean, rng: Random): DroneEvent = {
    val temperature = if (isFireEvent) 55.0 + rng.nextDouble() * 20.0 else 18.0 + rng.nextDouble() * 8.0
    val humidity = if (isFireEvent) 10.0 + rng.nextDouble() * 10.0 else 35.0 + rng.nextDouble() * 20.0
    val co2 = if (isFireEvent) 900.0 + rng.nextDouble() * 400.0 else 380.0 + rng.nextDouble() * 40.0
    val smoke = if (isFireEvent) 60.0 + rng.nextDouble() * 30.0 else rng.nextDouble() * 5.0
    DroneEvent(timestamp, profile.deviceId, profile.latitude, profile.longitude, temperature, humidity, co2, smoke)
  }

  def nextReading(profile: DeviceProfile, timestamp: Instant, rng: Random): DroneEvent =
    readingFor(profile, timestamp, rng.nextDouble() < fireProbability, rng)
}
