package controllers.action

import javax.inject.{Inject, Named}

import components.storage.Storage
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class UserAction @Inject() (
  @Named("sessionIdKey") sessionIdKey: String,
  @Named("sessionStorage") sessionStorage: Storage
)(implicit ec: ExecutionContext) extends ActionBuilder[UserRequest] {

  override def invokeBlock[A](
    request: Request[A],
    block: (UserRequest[A]) => Future[Result]
  ): Future[Result] = {

    val userRequest = request.cookies.get(sessionIdKey).map { cookie =>
      val sessionId = sessionStorage.touch(cookie.value)
      new UserRequest(sessionId, request)
    }.getOrElse {
      new UserRequest(sessionStorage.create(), request)
    }

    block(userRequest).map { result =>
      result.withCookies(Cookie(sessionIdKey, userRequest.sessionId))
    }
  }
}
