package alertservice

import akka.actor.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Keep, Source}
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
    val alert = EnrichedAlert(Instant.parse("2026-07-03T10:15:00Z"), "drone-001", 43.5, 5.2, "high temperature and smoke", "owner", "contact")

    dispatcher.dispatch(alert)

    probe.request(1)
    probe.expectNext(5.seconds, alert)
  }
}
