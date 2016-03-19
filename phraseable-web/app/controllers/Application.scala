package controllers

import javax.inject.Inject

import play.api.mvc.Controller

class Application @Inject() (
  pagesController: PagesController
) extends Controller {

  def index() = pagesController.page("index")
}
