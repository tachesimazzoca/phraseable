package controllers

import javax.inject.{Inject, Named}

import components.storage.Storage
import models._
import models.form._
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

class AccountController @Inject() (
  userAction: UserAction,
  @Named("verificationStorage") verificationStorage: Storage,
  idSequenceDao: IdSequenceDao,
  accountDao: AccountDao,
  userSession: UserSession,
  val messagesApi: MessagesApi
) extends Controller with I18nSupport {

  private val logger = Logger(this.getClass())

  private val form = AccountEntryForm.defaultForm

  def entry = userAction { implicit request =>
    Ok(views.html.account.entry(form))
  }

  def postEntry = userAction { implicit request =>
    form.bindFromRequest.fold(
      form => BadRequest(views.html.account.entry(form)),
      data => {
        accountDao.findByEmail(data.email).map { _ =>
          val formWithError = form.bind(
            form.mapping.unbind(data).updated("uniqueEmail", "false"))
          BadRequest(views.html.account.entry(formWithError))
        }.getOrElse {
          val verificadtionId = verificationStorage.create(form.mapping.unbind(data))
          logger.debug(verificadtionId)
          Redirect(routes.AccountController.verify())
        }
      }
    )
  }

  def verify = userAction {
    Ok(views.html.account.verify())
  }

  def activate = userAction { implicit request =>
    (for {
      code <- request.getQueryString("code")
      params <- verificationStorage.read(code)
      formMayErr = form.bind(params)
      data <- if (formMayErr.hasErrors) None else Some(formMayErr.get)
    } yield {
      accountDao.findByEmail(data.email).map { _ =>
        BadRequest
      }.getOrElse {
        val accountId = idSequenceDao.nextId(IdSequence.SequenceType.Account)
        val account = Account(accountId, data.email,
          Account.hashPassword(data.password), Account.Status.Active)
        val createdAccount = accountDao.create(account)
        Ok(views.html.account.activate(createdAccount))
      }
    }).getOrElse {
      BadRequest
    }
  }

  def error(name: String) = userAction {
    name match {
      case "session" =>
        Forbidden(views.html.account.error.session())
      case "email" =>
        Forbidden(views.html.account.error.email())
      case _ =>
        BadRequest
    }
  }
}
