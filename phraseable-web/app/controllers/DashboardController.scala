package controllers

import javax.inject.Inject

import play.api.mvc._

class DashboardController @Inject() (
  userAction: UserAction
) extends Controller {

  def index = userAction { implicit request =>
    println(request.data)
    Ok("ok")
  }
}
