package alertservice

import akka.stream.scaladsl.SourceQueueWithComplete

final class WebSocketNotificationDispatcher(queue: SourceQueueWithComplete[EnrichedAlert]) extends NotificationDispatcher {
  def dispatch(alert: EnrichedAlert): Unit = {
    val _ = queue.offer(alert)
  }
}
