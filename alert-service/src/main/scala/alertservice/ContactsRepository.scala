package alertservice

import io.circe.Decoder
import io.circe.parser.decode

import scala.io.Source

final case class ContactRecord(geohash: String, owner: String, contact: String)

object ContactRecord {
  implicit val decoder: Decoder[ContactRecord] = Decoder.forProduct3(
    "geohash", "owner", "contact"
  )(ContactRecord.apply)
}

object ContactsRepository {

  def load(resource: String): Map[String, Contact] = {
    val raw = Option(getClass.getClassLoader.getResourceAsStream(resource))
      .map(stream => Source.fromInputStream(stream).mkString)
      .getOrElse("[]")
    decode[List[ContactRecord]](raw)
      .getOrElse(Nil)
      .map(record => record.geohash -> Contact(record.owner, record.contact))
      .toMap
  }

  def lookup(contacts: Map[String, Contact], geohash: String): Option[Contact] =
    contacts.collectFirst { case (key, contact) if key == geohash => contact }
      .orElse(contacts.collectFirst { case (key, contact) if geohash.take(4) == key.take(4) => contact })
      .orElse(contacts.collectFirst { case (key, contact) if geohash.take(3) == key.take(3) => contact })
      .orElse(contacts.collectFirst { case (key, contact) if geohash.take(2) == key.take(2) => contact })
}
