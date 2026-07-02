package lakeingestion

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString

import java.nio.file.{Files, Paths}
import java.util.UUID
import scala.concurrent.Future

object BronzeWriter {

  def groupByPartition(root: String, batch: Seq[String]): Map[String, Seq[String]] =
    batch
      .flatMap(rawJson => BronzePartitioner.partitionFor(root, rawJson).map(path => path -> rawJson))
      .groupBy { case (path, _) => path }
      .map { case (path, entries) => path -> entries.map { case (_, rawJson) => rawJson } }

  def writePartition(partitionPath: String, records: Seq[String])(implicit system: ActorSystem): Future[Done] = {
    Files.createDirectories(Paths.get(partitionPath))
    val fileName = s"part-${System.currentTimeMillis()}-${UUID.randomUUID()}.json"
    val content = ByteString(records.mkString("", "\n", "\n"))
    Source.single(content).runWith(FileIO.toPath(Paths.get(partitionPath, fileName))).map(_ => Done)(system.dispatcher)
  }

  def writeBatch(root: String, batch: Seq[String])(implicit system: ActorSystem): Future[Done] = {
    import system.dispatcher
    Future.sequence(groupByPartition(root, batch).map { case (path, records) => writePartition(path, records) }).map(_ => Done)
  }
}
