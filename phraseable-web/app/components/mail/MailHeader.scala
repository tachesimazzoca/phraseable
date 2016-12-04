package components.mail

import play.api.Configuration

import scala.collection.JavaConversions._

object MailHeader {

  trait HeaderValue

  case class StringValue(value: String) extends HeaderValue

  case class AddressValue(address: String, personal: String = "") extends HeaderValue

  def fromConfiguration(config: Configuration): Map[String, Seq[HeaderValue]] = {

    val fromOpt = for {
      conf <- config.getConfig("From")
      address <- conf.getString("address")
    } yield ("From", Seq(AddressValue(address, conf.getString("personal").getOrElse(""))))

    def parseRecipients(name: String): Option[(String, Seq[HeaderValue])] =
      config.getConfigList(name).flatMap { cs =>
        val recipients = for {
          conf <- cs.toList
          address <- conf.getString("address")
        } yield AddressValue(address, conf.getString("personal").getOrElse(""))
        if (!recipients.isEmpty) Some((name, recipients)) else None
      }

    Seq(
      fromOpt,
      parseRecipients("To"),
      parseRecipients("Cc"),
      parseRecipients("Bcc")
    ).flatMap(_.toSeq).toMap
  }
}
