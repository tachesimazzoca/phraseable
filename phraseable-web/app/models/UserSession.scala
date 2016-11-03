package models

import javax.inject.Named

import com.google.inject.Inject
import components.storage.Storage

object UserSession {

  case class Data(id: Option[Long])

}

class UserSession @Inject() (
  @Named("sessionStorage") storage: Storage
) {

  import UserSession._

  def create(): String = storage.create()

  def read(key: String): Option[Data] =
    for {
      m <- storage.read(key)
      id <- m.get("id")
    } yield {
      val idOpt = if (id.isEmpty) None else Some(id.toLong)
      Data(idOpt)
    }

  def update(key: String, data: Data): Unit =
    storage.write(key, Map("id" -> data.id.toString))

  def delete(key: String): Unit = storage.delete(key)
}
