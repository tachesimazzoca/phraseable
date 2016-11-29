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

  private val form = AccountEntryForm.defaultForm

  def entry = Action { implicit request =>
    Ok(views.html.account.entry(form))
  }

  def postEntry = Action { implicit request =>
    form.bindFromRequest.fold(
      form => BadRequest(views.html.account.entry(form)),
      data => {
        accountDao.findByEmail(data.email).map { _ =>
          val formWithError = form.bind(form.mapping.unbind(data).updated("uniqueEmail", "false"))
          BadRequest(views.html.account.entry(formWithError))
        }.getOrElse {
          val id = idSequenceDao.nextId(IdSequence.SequenceType.Account)
          val account = Account(id, data.email, Account.hashPassword(data.password),
            Account.Status.Active)
          accountDao.create(account)
          Ok
        }
      }
    )
  }

  def verify = TODO

  def activate = TODO
}
