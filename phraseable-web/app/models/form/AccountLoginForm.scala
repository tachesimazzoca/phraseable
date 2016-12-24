package models.form

import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data._

case class AccountLoginForm(email: String, password: String)

object AccountLoginForm {

  private val form = Form(
    mapping(
      "email" -> text,
      "password" -> text,
      "authorized" -> default(of[Boolean], true).verifying(
        "AccountLoginForm.error.authorized",
        _ == true
      )
    ) { (email, password, _) =>
      AccountLoginForm(email, password)
    } { a =>
      Some(a.email, a.password, true)
    }
  )

  def defaultForm: Form[AccountLoginForm] = form

  def update(data: AccountLoginForm, params: Map[String, String]): Form[AccountLoginForm] = {
    form.bind(form.mapping.unbind(data) ++ params)
  }

  def deauthorize(data: AccountLoginForm): Form[AccountLoginForm] = update(data, Map("authorized" -> "false"))
}
