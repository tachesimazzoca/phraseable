package components.storage

import java.net.{URLDecoder, URLEncoder}

class Storage(engine: StorageEngine, namespace: String = "") {

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
    val key = namespace + java.util.UUID.randomUUID().toString
    engine.write(key, serialize(data))
    key
  }

  def read(key: String): Option[Map[String, String]] =
    engine.read(key).map { bytes =>
      unserialize(bytes)
    }

  def write(key: String, data: Map[String, String]): Unit = engine.write(key, serialize(data))

  def delete(key: String): Unit = engine.delete(key)
}
