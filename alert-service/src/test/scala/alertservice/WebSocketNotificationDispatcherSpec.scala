package alertservice

import akka.actor.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant
import scala.concurrent.duration._

class WebSocketNotificationDispatcherSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  private implicit val system: ActorSystem = ActorSystem("websocket-notification-dispatcher-spec")

  override def afterAll(): Unit = {
    val _ = system.terminate()
  }

  "dispatch" should "push the alert to subscribers of the underlying stream" in {
    val (queue, probe) = Source
      .queue[EnrichedAlert](10, OverflowStrategy.dropHead)
      .toMat(TestSink.probe[EnrichedAlert])(Keep.both)
      .run()

    val dispatcher = new WebSocketNotificationDispatcher(queue)
    val alert = EnrichedAlert(Instant.parse("2026-07-03T10:15:00Z"), "drone-001", 43.5, 5.2, 60.0, 70.0, 950.0, 15.0, "high temperature and smoke", "owner", "contact")

    dispatcher.dispatch(alert)

    probe.request(1)
    probe.expectNext(5.seconds, alert)
  }

  it should "broadcast the same alert to multiple concurrent subscribers via a BroadcastHub" in {
    val (queue, broadcastSource) = Source
      .queue[EnrichedAlert](10, OverflowStrategy.dropHead)
      .toMat(BroadcastHub.sink[EnrichedAlert](bufferSize = 16))(Keep.both)
      .run()

    val probe1 = broadcastSource.runWith(TestSink.probe[EnrichedAlert])
    val probe2 = broadcastSource.runWith(TestSink.probe[EnrichedAlert])

    val dispatcher = new WebSocketNotificationDispatcher(queue)
    val alert = EnrichedAlert(Instant.parse("2026-07-03T10:15:00Z"), "drone-002", 44.0, 6.0, 60.0, 70.0, 950.0, 15.0, "high temperature and smoke", "owner", "contact")

    probe1.request(1)
    probe2.request(1)

    dispatcher.dispatch(alert)

    probe1.expectNext(5.seconds, alert)
    probe2.expectNext(5.seconds, alert)
  }
}
