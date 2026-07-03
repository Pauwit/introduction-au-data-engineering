package alertservice

object ConsoleNotificationDispatcher extends NotificationDispatcher {

  def format(alert: EnrichedAlert): String =
    s"[ALERT] ${alert.timestamp} device=${alert.deviceId} reason=${alert.reason} owner=${alert.owner} contact=${alert.contact}"

  def dispatch(alert: EnrichedAlert): Unit =
    println(format(alert))
}
