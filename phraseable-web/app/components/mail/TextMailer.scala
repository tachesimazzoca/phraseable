package components.mail

import javax.inject.Inject

import components.mail.MailHeader._
import org.apache.commons.mail.SimpleEmail
import play.api.Logger

class TextMailer @Inject() (
  relayHost: RelayHost
) extends Mailer[String] {

  private val logger = Logger(this.getClass())

  private def parseSingleStringValue(value: Option[Seq[HeaderValue]]): Option[String] =
    value.headOption.flatMap { xs =>
      xs(0) match {
        case StringValue(x) => Some(x)
        case _ => None
      }
    }

  private def parseMultiAddressValue(value: Option[Seq[HeaderValue]]): Option[Seq[AddressValue]] = {
    value.flatMap {
      case xs: Seq[AddressValue] => Some(xs)
      case _ => None
    }
  }

  override def send(headers: Map[String, Seq[HeaderValue]], content: String): String = {
    val email = new SimpleEmail()

    parseSingleStringValue(headers.get("Charset")).foreach(email.setCharset)
    parseSingleStringValue(headers.get("Subject")).foreach(email.setSubject)
    parseMultiAddressValue(headers.get("From")).foreach { xs =>
      xs.headOption.foreach(x => email.setFrom(x.address, x.personal))
    }
    parseMultiAddressValue(headers.get("To")).foreach { xs =>
      xs.foreach(x => email.addTo(x.address, x.personal))
    }
    parseMultiAddressValue(headers.get("Cc")).foreach { xs =>
      xs.foreach(x => email.addCc(x.address, x.personal))
    }
    parseMultiAddressValue(headers.get("Bcc")).foreach { xs =>
      xs.foreach(x => email.addBcc(x.address, x.personal))
    }

    // TODO: Set additional headers ...

    email.setMsg(content)

    relayHost match {
      case MockRelayHost =>
        logger.debug(headers.toString())
        ""
      case SMTPRelayHost(host, port) =>
        email.setHostName(host)
        email.setSmtpPort(port)
        email.send
    }
  }
}
