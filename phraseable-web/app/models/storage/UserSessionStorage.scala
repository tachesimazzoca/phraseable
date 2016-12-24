package models.storage

import javax.inject.{Inject, Named}

import components.storage.Storage

object UserSession {

  val KEY_ACCOUNT_ID = "UserSession.accountId"

  def defaultData = UserSession(None)
}

case class UserSession(accountId: Option[Long])

class UserSessionStorage @Inject() (
  @Named("sessionStorage") storage: Storage
) {

  import UserSession._

  def create(): String = storage.create()

  def read(key: String): Option[UserSession] =
    for {
      m <- storage.read(key)
      accountId <- m.get(KEY_ACCOUNT_ID)
    } yield {
      UserSession(if (accountId.isEmpty) None else Some(accountId.toLong))
    }

  def update(key: String, data: UserSession): Unit = {
    val m = storage.read(key).getOrElse(Map.empty)
    storage.write(key, m ++ Map(
      KEY_ACCOUNT_ID -> data.accountId.map(_.toString).getOrElse("")))
  }

  def delete(key: String): Unit = {
    val m = storage.read(key).getOrElse(Map.empty)
    storage.write(key, m - KEY_ACCOUNT_ID)
  }
}
