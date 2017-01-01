package controllers.action

import javax.inject.Inject

import controllers.routes
import controllers.session.UserSessionFactory
import models.{Account, AccountDao}
import play.api.mvc.Results._
import play.api.mvc.{ActionRefiner, Result}

import scala.concurrent.Future

class MemberAction @Inject() (
  userSessionFactory: UserSessionFactory,
  accountDao: AccountDao
) extends ActionRefiner[UserRequest, MemberRequest] {

  override protected def refine[A](
    request: UserRequest[A]
  ): Future[Either[Result, MemberRequest[A]]] = Future.successful {

    val userLoginSession = userSessionFactory.create("UserLogin")
      .read(request.sessionId)

    val accountOpt = for {
      idString <- userLoginSession.get("accountId")
      a <- accountDao.find(idString.toLong) if a.status == Account.Status.Active
    } yield a

    accountOpt.map { account =>
      Right(new MemberRequest(account, request))
    }.getOrElse {
      val returnTo =
        if (request.method == "GET") Some(request.uri)
        else None
      Left(Redirect(routes.AccountController.login(returnTo)))
    }
  }
}
