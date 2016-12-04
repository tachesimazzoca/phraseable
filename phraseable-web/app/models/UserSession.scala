package models

import javax.inject.{Inject, Named}

import components.storage.Storage

object UserSession {

  case class Data(accountId: Option[Long])

}

class UserSession @Inject() (
  @Named("sessionStorage") storage: Storage
) {

  import UserSession._

  def create(): String = storage.create()

  def read(key: String): Option[Data] =
    for {
      m <- storage.read(key)
      accountId <- m.get("accountId")
    } yield {
      Data(if (accountId.isEmpty) None else Some(accountId.toLong))
    }

  def update(key: String, data: Data): Unit = {
    storage.write(key, Map("id" -> data.accountId.toString))
  }

  def delete(key: String): Unit = storage.delete(key)
}
