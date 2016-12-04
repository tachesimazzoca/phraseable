package components.mail

import components.mail.MailHeader._
import org.scalatest.FunSuite

class TextMailerSuite extends FunSuite {
  test("send") {
    val mailer = new TextMailer(MockRelayHost)
    val headers = Map(
      "Charset" -> Seq(StringValue("ISO-2022-JP")),
      "To" -> Seq(AddressValue("alice@example.net"), AddressValue("bob@example.net"))
    )
    mailer.send(headers, "No messages")
  }
}
