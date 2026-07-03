package alertservice

trait NotificationDispatcher {
  def dispatch(alert: EnrichedAlert): Unit
}
