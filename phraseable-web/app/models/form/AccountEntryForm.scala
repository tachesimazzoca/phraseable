package models.form

import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._

case class AccountEntryForm(email: String, password: String)

object AccountEntryForm {

  private val form = Form(
    mapping(
      "email" -> text.verifying(emailAddress),
      "password" -> tuple(
        "main" -> text.verifying(
          "AccountEntryForm.error.password",
          _.matches( """^.{8,64}$""")
        ),
        "confirmation" -> text
      ).verifying(
        "AccountEntryForm.error.retypedPassword",
        passwords => passwords._1 == passwords._2
      ),
      "uniqueEmail" -> default(of[Boolean], true).verifying(
        "AccountEntryForm.error.uniqueEmail",
        _ == true
      )

    ) { (email, passwords, _) =>
      AccountEntryForm(email, passwords._1)
    } { a =>
      Some(a.email, (a.password, a.password), true)
    }
  )

  def defaultForm: Form[AccountEntryForm] = form
}
