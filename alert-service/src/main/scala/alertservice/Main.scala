package alertservice

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.TextMessage
import akka.http.scaladsl.server.Directives._
import akka.kafka.ConsumerSettings
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.pattern.ask
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, Sink, Source}
import akka.util.Timeout
import io.circe.parser.decode
import io.circe.syntax._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.duration._

object Main {

  def main(args: Array[String]): Unit = {
    val env = sys.env

    implicit val system: ActorSystem = ActorSystem("alert-service")
    import system.dispatcher
    implicit val contactsTimeout: Timeout = Timeout(5.seconds)

    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(AlertServiceConfig.bootstrapServers(env))
      .withGroupId("alert-service")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

    val alerts = Consumer
      .plainSource(consumerSettings, Subscriptions.topics(AlertServiceConfig.alertsTopic(env)))
      .map(record => decode[Alert](record.value()))
      .collect { case Right(alert) => alert }

    val contactsCache = system.actorOf(ContactsCacheActor.props(AlertServiceConfig.contactsResource(env)))
    val refreshInterval = AlertServiceConfig.contactsRefreshInterval(env)

    val _ = Source
      .tick(refreshInterval, refreshInterval, ContactsCacheActor.Reload)
      .runForeach(contactsCache ! _)

    val precision = AlertServiceConfig.geohashPrecision(env)

    val enrichedAlerts = alerts.mapAsync(4) { alert =>
      (contactsCache ? ContactsCacheActor.GetContacts)
        .mapTo[Map[String, Contact]]
        .map(contacts => AlertEnricher.enrich(alert, contacts, precision))
    }

    val (queue, broadcastSource) = Source
      .queue[EnrichedAlert](256, OverflowStrategy.dropHead)
      .toMat(BroadcastHub.sink[EnrichedAlert](bufferSize = 256))(Keep.both)
      .run()

    val dispatchers = List(ConsoleNotificationDispatcher, new WebSocketNotificationDispatcher(queue))

    val _ = enrichedAlerts.runForeach(alert => dispatchers.foreach(_.dispatch(alert)))

    val alertsRoute = path("alerts") {
      handleWebSocketMessages(Flow.fromSinkAndSource(Sink.ignore, broadcastSource.map(alert => TextMessage(alert.asJson.noSpaces))))
    }

    val healthRoute = path("health") {
      get {
        complete("ok")
      }
    }

    val _ = Http().newServerAt(AlertServiceConfig.httpHost(env), AlertServiceConfig.httpPort(env)).bind(alertsRoute ~ healthRoute)
  }
}
