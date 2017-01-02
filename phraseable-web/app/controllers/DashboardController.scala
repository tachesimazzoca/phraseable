package controllers

import javax.inject.Inject

import controllers.action.{MemberAction, UserAction}
import models.AccountAccessDao
import play.api.mvc._

class DashboardController @Inject() (
  userAction: UserAction,
  memberAction: MemberAction,
  accountAccessDao: AccountAccessDao
) extends Controller {

  def index = (userAction andThen memberAction) {
    Ok(views.html.dashboard.index())
  }

  def access = (userAction andThen memberAction) { implicit memberRequest =>
    Ok(views.html.dashboard.access(
      accountAccessDao.selectByAccountId(memberRequest.account.id)))
  }
}
