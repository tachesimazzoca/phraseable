package controllers.action

import models.storage.UserSession
import play.api.mvc._

class UserRequest[A](
  val sessionId: String,
  val userSession: Option[UserSession],
  request: Request[A]
) extends WrappedRequest[A](request)
