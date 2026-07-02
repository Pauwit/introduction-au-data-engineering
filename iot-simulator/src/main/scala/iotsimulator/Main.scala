package iotsimulator

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import io.circe.syntax._
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import java.time.Instant
import scala.concurrent.duration._
import scala.util.Random

object Main {

  def main(args: Array[String]): Unit = {
    val env = sys.env

    implicit val system: ActorSystem = ActorSystem("iot-simulator")

    val devices = DeviceProfile.simulatedFleet(SimulatorConfig.deviceCount(env))
    val rng = new Random()
    val topic = SimulatorConfig.topic(env)

    val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
      .withBootstrapServers(SimulatorConfig.bootstrapServers(env))

    val _ = Source
      .tick(0.seconds, SimulatorConfig.tickInterval(env), devices)
      .mapConcat(identity)
      .map(profile => SensorReadingGenerator.nextReading(profile, Instant.now(), rng))
      .zipWithIndex
      .map { case (event, index) =>
        println(s"sending event #$index for ${event.deviceId}")
        new ProducerRecord[String, String](topic, event.deviceId, event.asJson.noSpaces)
      }
      .runWith(Producer.plainSink(producerSettings))
  }
}
