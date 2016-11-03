package controllers

import models.Account
import play.api.mvc._

object UserRequest {
  case class Data(sessionId: String, account: Option[Account])
}

class UserRequest[A](val data: UserRequest.Data, request: Request[A]) extends WrappedRequest[A](request)
