package controllers.action

import javax.inject.{Inject, Named}

import models.storage.UserSessionStorage
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class UserAction @Inject() (
  @Named("sessionIdKey") sessionIdKey: String,
  userSessionStorage: UserSessionStorage
)(implicit ec: ExecutionContext) extends ActionBuilder[UserRequest] {

  override def invokeBlock[A](
    request: Request[A],
    block: (UserRequest[A]) => Future[Result]
  ): Future[Result] = {

    val userRequest = request.cookies.get(sessionIdKey).flatMap { cookie =>
      val sessId = cookie.value
      userSessionStorage.read(sessId).map { data =>
        userSessionStorage.update(sessId, data)
        Some(new UserRequest(sessId, Some(data), request))
      }.getOrElse {
        None
      }
    }.getOrElse {
      new UserRequest(userSessionStorage.create(), None, request)
    }

    block(userRequest).map { result =>
      result.withCookies(Cookie(
        sessionIdKey, userRequest.sessionId))
    }
  }
}
