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

    val userRequest = request.cookies.get(sessionIdKey).map { cookie =>
      val sessId = cookie.value
      // TODO: Regenerate sessionId
      new UserRequest(sessId, userSessionStorage.read(sessId), request)
    }.getOrElse {
      new UserRequest(userSessionStorage.create(), None, request)
    }

    block(userRequest).map { result =>
      result.withCookies(Cookie(
        sessionIdKey, userRequest.sessionId))
    }
  }
}
