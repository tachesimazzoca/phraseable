package controllers

import javax.inject.Inject

import play.api.mvc.{Action, Controller}

import models._

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
