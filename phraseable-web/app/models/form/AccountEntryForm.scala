package models.form

import play.api.data.Forms._
import play.api.data._

case class AccountEntryForm(username: String, password: String)

object AccountEntryForm {
  private val form = Form(
    mapping(
      "username" -> text.verifying(
        "AccountEntryForm.error.username",
        _.matches( """^([a-z][0-9a-z]{1,31})$""")
      ),
      "password" -> tuple(
        "main" -> text.verifying(
          "AccountEntryForm.error.password",
          _.matches( """^.{8,64}$""")
        ),
        "confirmation" -> text
      ).verifying(
          "AccountEntryForm.error.passwords",
          passwords => passwords._1 == passwords._2
        )
    ) { (username, passwords) =>
      AccountEntryForm(username, passwords._1)
    } { a =>
      Some(a.username, (a.password, a.password))
    }
  )

  def defaultForm: Form[AccountEntryForm] = form
}
