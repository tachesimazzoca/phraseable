package controllers

import javax.inject.{Inject, Named}

import models._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class UserAction @Inject() (
  @Named("sessionIdKey") sessionIdKey: String,
  userSession: UserSession,
  accountDao: AccountDao
)(implicit ec: ExecutionContext) extends ActionBuilder[UserRequest] {

  override def invokeBlock[A](
    request: Request[A], block: (UserRequest[A]) => Future[Result]): Future[Result] = {

    val userRequestData = request.cookies.get(sessionIdKey).map { cookie =>
      val sessionId = cookie.value
      val account = for {
        data <- userSession.read(sessionId)
        id <- data.id
        a <- accountDao.find(id)
      } yield a
      // TODO: Re-generate session Id to refuse session fixation attacks.
      UserRequest.Data(sessionId, account)
    }.getOrElse {
      UserRequest.Data(userSession.create(), None)
    }
    block(new UserRequest(userRequestData, request)).map { result =>
      result.withCookies(Cookie(sessionIdKey, userRequestData.sessionId))
    }
  }
}
