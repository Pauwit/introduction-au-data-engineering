package alertservice

import akka.actor.{Actor, Props}

object ContactsCacheActor {
  case object Reload
  case object GetContacts

  def props(resource: String): Props = Props(new ContactsCacheActor(resource))
}

final class ContactsCacheActor(resource: String) extends Actor {
  import ContactsCacheActor._

  override def receive: Receive = ready(ContactsRepository.load(resource))

  private def ready(contacts: Map[String, Contact]): Receive = {
    case Reload => context.become(ready(ContactsRepository.load(resource)))
    case GetContacts => sender() ! contacts
  }
}
