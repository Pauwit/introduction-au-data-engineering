package alertservice

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class ContactsCacheActorSpec extends TestKit(ActorSystem("contacts-cache-actor-spec")) with ImplicitSender with AnyFlatSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    val _ = TestKit.shutdownActorSystem(system)
  }

  "ContactsCacheActor" should "load contacts on start and answer GetContacts" in {
    val actor = system.actorOf(ContactsCacheActor.props("src/test/resources/test-contacts.json"))

    actor ! ContactsCacheActor.GetContacts

    expectMsg(ContactsRepository.load("src/test/resources/test-contacts.json"))
  }

  it should "reload on demand and answer with the freshly loaded contacts" in {
    val actor = system.actorOf(ContactsCacheActor.props("src/test/resources/test-contacts.json"))

    actor ! ContactsCacheActor.Reload
    actor ! ContactsCacheActor.GetContacts

    expectMsg(ContactsRepository.load("src/test/resources/test-contacts.json"))
  }
}
