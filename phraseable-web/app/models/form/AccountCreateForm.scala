package models.form

import org.apache.commons.lang3.StringUtils
import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._

case class AccountCreateForm(email: String, password: String)

object AccountCreateForm extends NormalizationSupport {

  import ConstraintHelper._

  private val form = Form(
    mapping(
      "email" -> text.verifying(email("AccountCreateForm.error.email")),
      "password" -> tuple(
        "main" -> text.verifying(password("AccountCreateForm.error.password")),
        "confirmation" -> text
      ).verifying(sameValue("AccountCreateForm.error.retypedPassword")),
      "uniqueEmail" -> default(of[Boolean], true).verifying(
        passed("AccountCreateForm.error.uniqueEmail"))
    ) { (email, passwords, _) =>
      AccountCreateForm(email, passwords._1)
    } { a =>
      Some(a.email, (a.password, a.password), true)
    }
  )

  def defaultForm: Form[AccountCreateForm] = form

  def fromRequest(implicit request: play.api.mvc.Request[_]): Form[AccountCreateForm] =
    form.bindFromRequest(normalize(request))

  override def normalize(data: Map[String, Seq[String]]): Map[String, Seq[String]] = {
    // Trim white spaces
    Seq("email", "password.main", "password.confirmation").foldRight(data) { (x, acc) =>
      acc.updated(x, data(x).map(StringUtils.stripToEmpty))
    }
  }
}
