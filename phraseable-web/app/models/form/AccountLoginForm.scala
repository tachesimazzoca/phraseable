package models.form

import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._

case class AccountLoginForm(email: String, password: String)

object AccountLoginForm extends NormalizationSupport {

  import ConstraintHelper._

  override val nonBlankFields: Seq[String] = Seq("email", "password")

  private val form = Form(
    mapping(
      "email" -> text,
      "password" -> text,
      "authorized" -> default(of[Boolean], true)
        .verifying(passed("AccountLoginForm.error.authorized"))
    ) { (email, password, _) =>
      AccountLoginForm(email, password)
    } { a =>
      Some(a.email, a.password, true)
    }
  )

  def defaultForm: Form[AccountLoginForm] = form

  def fromRequest(implicit request: play.api.mvc.Request[_]): Form[AccountLoginForm] =
    form.bindFromRequest(normalize(request))

  def update(data: AccountLoginForm, params: Map[String, String]): Form[AccountLoginForm] = {
    form.bind(form.mapping.unbind(data) ++ params)
  }

  def deauthorize(data: AccountLoginForm): Form[AccountLoginForm] =
    update(data, Map("authorized" -> "false"))
}
