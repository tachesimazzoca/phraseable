package controllers

import javax.inject.Inject

import models.AccountDao
import play.api.mvc.{Action, Controller}

class AccountsController @Inject() (
  accountDao: AccountDao
) extends Controller {

  def get(id: Long) = Action {
    accountDao.find(id).map { account =>
      Ok(account.toString)
    }.getOrElse {
      NotFound
    }
  }
}
