package models

import javax.inject.Named

import com.google.inject.Inject
import components.storage.Storage

object UserSession {

  case class Data(id: Long)

}

class UserSession @Inject() (
  @Named("sessionStorage") storage: Storage
) {

  import UserSession._

  def create(data: Data): String = {
    storage.create(Map("id" -> data.id.toString))
  }

  def find(key: String): Option[Data] =
    for {
      m <- storage.read(key)
      id <- m.get("id")
    } yield {
      Data(id.toLong)
    }

  def delete(key: String): Unit = storage.delete(key)
}
