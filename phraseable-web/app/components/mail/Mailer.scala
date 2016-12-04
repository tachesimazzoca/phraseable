package components.mail

import components.mail.MailHeader.HeaderValue

trait Mailer[T] {
  def send(headers: Map[String, Seq[HeaderValue]], content: T): String
}
