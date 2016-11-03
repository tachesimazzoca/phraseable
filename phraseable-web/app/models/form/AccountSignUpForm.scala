package models.form

import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._

case class AccountSignUpForm(username: String, password: String)

object AccountSignUpForm {

  private val form = Form(
    mapping(
      "username" -> text.verifying(
        "AccountSignUpForm.error.username",
        _.matches( """^([a-z][0-9a-z]{4,31})$""")
      ),
      "password" -> tuple(
        "main" -> text.verifying(
          "AccountSignUpForm.error.password",
          _.matches( """^.{8,64}$""")
        ),
        "confirmation" -> text
      ).verifying(
        "AccountSignUpForm.error.retypedPassword",
        passwords => passwords._1 == passwords._2
      ),
      "uniqueUsername" -> default(of[Boolean], true).verifying(
        "AccountSignUpForm.error.uniqueUsername",
        _ == true
      )

    ) { (username, passwords, _) =>
      AccountSignUpForm(username, passwords._1)
    } { a =>
      Some(a.username, (a.password, a.password), true)
    }
  )

  def defaultForm: Form[AccountSignUpForm] = form
}
