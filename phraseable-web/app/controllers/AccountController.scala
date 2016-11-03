package controllers

import javax.inject.Inject

import models._
import models.form._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

class AccountController @Inject() (
  accountDao: AccountDao,
  idSequenceDao: IdSequenceDao,
  val messagesApi: MessagesApi
) extends Controller with I18nSupport {

  private val form = AccountSignUpForm.defaultForm

  def signUp = Action { implicit request =>
    Ok(views.html.account.signUp(form))
  }

  def postSignUp = Action { implicit request =>
    form.bindFromRequest.fold(
      form => BadRequest(views.html.account.signUp(form)),
      data => {
        accountDao.findByUsername(data.username).map { _ =>
          // The account.username is not unique.
          val formWithError = form.bind(form.mapping.unbind(data).updated("uniqueUsername", "false"))
          BadRequest(views.html.account.signUp(formWithError))
        }.getOrElse {
          val id = idSequenceDao.nextId(IdSequence.SequenceType.Account)
          val account = Account(id, data.username, Account.hashPassword(data.password),
            Account.Status.Active, "")
          accountDao.create(account)
          Ok
        }
      }
    )
  }
}
