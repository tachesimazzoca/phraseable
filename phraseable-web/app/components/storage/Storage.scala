package components.storage

import java.net.{URLDecoder, URLEncoder}

import components.util.{Chance, Chances}

object Storage {

  case class Settings(
    namespace: String = "",
    gcMaxLifetime: Option[Long] = None,
    gcChance: Chance = Chances.random(1, 100)
  )

}

class Storage(engine: StorageEngine, settings: Storage.Settings = Storage.Settings()) {

  private val encoding = "UTF-8"

  private def encode(str: String): String = URLEncoder.encode(str, encoding)

  private def decode(str: String): String = URLDecoder.decode(str, encoding)

  private def serialize(data: Map[String, String]): Array[Byte] =
    data.map { case (k, v) =>
      encode(k) + "=" + encode(v)
    }.mkString("&").getBytes

  private def unserialize(bytes: Array[Byte]): Map[String, String] = {
    new String(bytes, encoding).split("&")
      .map(_.split("=", 2))
      .map(p => (decode(p(0)), decode(p(1))))
      .toMap
  }

  def create(data: Map[String, String]): String = {
    gc()
    val key = settings.namespace + java.util.UUID.randomUUID().toString
    engine.write(key, serialize(data))
    key
  }

  def read(key: String): Option[Map[String, String]] = {
    gc()
    engine.read(key).map { bytes =>
      unserialize(bytes)
    }
  }

  def write(key: String, data: Map[String, String]): Unit = {
    gc()
    engine.write(key, serialize(data))
  }

  def delete(key: String): Unit = {
    gc()
    engine.delete(key)
  }

  def gc(): Unit = settings.gcMaxLifetime.foreach { lifetime =>
    if (settings.gcChance.yes()) {
      engine.gc(lifetime)
    }
  }
}
