package lakeingestion

import akka.actor.ActorSystem
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Path}
import java.util.Comparator
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class BronzeWriterSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  private implicit val system: ActorSystem = ActorSystem("bronze-writer-spec")

  override def afterAll(): Unit = {
    val _ = system.terminate()
  }

  "groupByPartition" should "drop malformed payloads and group the rest by partition, preserving order" in {
    val hour10First = """{"timestamp":"2026-07-03T10:15:30Z","device_id":"drone-001","latitude":43.5,"longitude":5.2,"temperature":21.4,"humidity":40.0,"co2":410.0,"smoke":1.2}"""
    val hour11First = """{"timestamp":"2026-07-03T11:05:00Z","device_id":"drone-002","latitude":44.0,"longitude":6.0,"temperature":19.0,"humidity":38.0,"co2":400.0,"smoke":0.8}"""
    val malformed = """{"timestamp":"2026-07-03T10:15:30Z"invalid}"""
    val hour10Second = """{"timestamp":"2026-07-03T10:45:00Z","device_id":"drone-003","latitude":43.6,"longitude":5.3,"temperature":22.0,"humidity":41.0,"co2":415.0,"smoke":1.5}"""
    val hour11Second = """{"timestamp":"2026-07-03T11:50:00Z","device_id":"drone-004","latitude":44.1,"longitude":6.1,"temperature":18.5,"humidity":37.0,"co2":398.0,"smoke":0.6}"""

    val batch = Seq(hour10First, hour11First, malformed, hour10Second, hour11Second)
    val grouped = BronzeWriter.groupByPartition("data-lake", batch)

    grouped.keySet shouldEqual Set(
      "data-lake/bronze/drone-events/date=2026-07-03/hour=10",
      "data-lake/bronze/drone-events/date=2026-07-03/hour=11"
    )
    grouped("data-lake/bronze/drone-events/date=2026-07-03/hour=10") shouldEqual Seq(hour10First, hour10Second)
    grouped("data-lake/bronze/drone-events/date=2026-07-03/hour=11") shouldEqual Seq(hour11First, hour11Second)
  }

  "writeBatch" should "write every payload of a batch into the file(s) of its bronze partition" in {
    val tempRoot = Files.createTempDirectory("bronze-writer-spec")

    val payloadOne = """{"timestamp":"2026-07-03T10:15:30Z","device_id":"drone-001","latitude":43.5,"longitude":5.2,"temperature":21.4,"humidity":40.0,"co2":410.0,"smoke":1.2}"""
    val payloadTwo = """{"timestamp":"2026-07-03T10:20:00Z","device_id":"drone-002","latitude":43.6,"longitude":5.3,"temperature":21.6,"humidity":39.5,"co2":412.0,"smoke":1.1}"""
    val payloadThree = """{"timestamp":"2026-07-03T10:55:00Z","device_id":"drone-003","latitude":43.7,"longitude":5.4,"temperature":21.8,"humidity":39.0,"co2":413.0,"smoke":1.0}"""
    val batch = Seq(payloadOne, payloadTwo, payloadThree)

    val result = Await.result(BronzeWriter.writeBatch(tempRoot.toString, batch), 10.seconds)
    result shouldEqual akka.Done

    val partitionDir = Path.of(tempRoot.toString, "bronze", "drone-events", "date=2026-07-03", "hour=10")
    Files.exists(partitionDir) shouldEqual true

    val writtenFiles = Files.list(partitionDir).iterator().asScala.toList
    writtenFiles should not be empty

    val combinedContent = writtenFiles.map(path => new String(Files.readAllBytes(path))).mkString

    batch.foreach(payload => combinedContent should include(payload))

    Files
      .walk(tempRoot)
      .sorted(Comparator.reverseOrder())
      .iterator()
      .asScala
      .foreach(Files.delete)
  }
}
