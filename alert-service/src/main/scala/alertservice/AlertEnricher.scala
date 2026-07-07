package alertservice

object AlertEnricher {

  private val unknown = "unknown"

  def enrich(alert: Alert, contacts: Map[String, Contact], geohashPrecision: Int): EnrichedAlert = {
    val geohash = Geohash.encode(alert.latitude, alert.longitude, geohashPrecision)
    val contact = ContactsRepository.lookup(contacts, geohash)

    EnrichedAlert(
      alert.timestamp,
      alert.deviceId,
      alert.latitude,
      alert.longitude,
      alert.temperature,
      alert.smoke,
      alert.co2,
      alert.humidity,
      alert.reason,
      contact.map(_.owner).getOrElse(unknown),
      contact.map(_.contact).getOrElse(unknown)
    )
  }
}
