package controllers

import javax.inject.Inject

import controllers.action.{MemberAction, UserAction}
import play.api.mvc._

class DashboardController @Inject() (
  userAction: UserAction,
  memberAction: MemberAction
) extends Controller {

  def index = (userAction andThen memberAction) { implicit memberRequest =>
    println(memberRequest.account)
    Ok("ok")
  }
}
