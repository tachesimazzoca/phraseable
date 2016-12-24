package models.form

import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._

case class AccountCreateForm(email: String, password: String)

object AccountCreateForm {

  private val form = Form(
    mapping(
      "email" -> text.verifying(emailAddress),
      "password" -> tuple(
        "main" -> text.verifying(
          "AccountCreateForm.error.password",
          _.matches( """^.{8,64}$""")
        ),
        "confirmation" -> text
      ).verifying(
        "AccountCreateForm.error.retypedPassword",
        passwords => passwords._1 == passwords._2
      ),
      "uniqueEmail" -> default(of[Boolean], true).verifying(
        "AccountCreateForm.error.uniqueEmail",
        _ == true
      )

    ) { (email, passwords, _) =>
      AccountCreateForm(email, passwords._1)
    } { a =>
      Some(a.email, (a.password, a.password), true)
    }
  )

  def defaultForm: Form[AccountCreateForm] = form
}
