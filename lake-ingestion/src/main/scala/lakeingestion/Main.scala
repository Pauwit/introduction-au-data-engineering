package lakeingestion

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.duration._

object Main {

  def main(args: Array[String]): Unit = {
    val env = sys.env

    implicit val system: ActorSystem = ActorSystem("lake-ingestion")

    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(LakeIngestionConfig.bootstrapServers(env))
      .withGroupId("lake-ingestion")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val root = LakeIngestionConfig.dataLakeRoot(env)
    val batchInterval = LakeIngestionConfig.batchIntervalSeconds(env).seconds

    val _ = Consumer
      .plainSource(consumerSettings, Subscriptions.topics(LakeIngestionConfig.inputTopic(env)))
      .map(_.value())
      .groupedWithin(LakeIngestionConfig.batchSize(env), batchInterval)
      .zipWithIndex
      .mapAsync(1) { case (batch, index) =>
        BronzeWriter.writeBatch(root, batch).map { done =>
          println(s"stored batch #$index (${batch.size} events) in $root")
          done
        }(system.dispatcher)
      }
      .runWith(Sink.ignore)
  }
}
