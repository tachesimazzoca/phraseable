package controllers

import javax.inject.Inject

import models._
import models.form._
import play.api.db.Database
import play.api.mvc._

class AccountController @Inject() (
  db: Database,
  accountDao: AccountDao,
  idSequenceDao: IdSequenceDao
) extends Controller {

  private val accountEntryForm = AccountEntryForm.defaultForm

  def postEntry = Action { implicit request =>
    accountEntryForm.bindFromRequest.fold(
      form => BadRequest,
      data => {
        db.withTransaction { implicit conn =>
          accountDao.findByUsername(data.username).map { _ =>
            BadRequest
          }.getOrElse {
            val id = idSequenceDao.nextId(IdSequence.SequenceType.Account)
            val account = Account(id, data.username, Account.hashPassword(data.password),
              Account.Status.Active, "")
            accountDao.create(account)
            Ok
          }
        }
      }
    )
  }
}
