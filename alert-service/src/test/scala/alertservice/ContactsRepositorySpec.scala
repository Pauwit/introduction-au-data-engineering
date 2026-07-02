package alertservice

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ContactsRepositorySpec extends AnyFlatSpec with Matchers {

  "load" should "read contacts from a classpath resource keyed by geohash" in {
    val contacts = ContactsRepository.load("test-contacts.json")

    contacts should have size 2
  }

  it should "return an empty map for a missing resource" in {
    val contacts = ContactsRepository.load("does-not-exist.json")

    contacts shouldEqual Map.empty
  }

  "lookup" should "find a contact by exact geohash" in {
    val contacts = ContactsRepository.load("test-contacts.json")

    ContactsRepository.lookup(contacts, "u4pru") shouldEqual Some(Contact("Office National des Forets - Vosges", "+33 3 88 00 11 22"))
  }

  it should "return none for an unknown geohash" in {
    val contacts = ContactsRepository.load("test-contacts.json")

    ContactsRepository.lookup(contacts, "zzzzz") shouldEqual None
  }
}
