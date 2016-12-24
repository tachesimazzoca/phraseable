package controllers.action

import javax.inject.Inject

import controllers.routes
import models.{Account, AccountDao}
import play.api.mvc.Results._
import play.api.mvc.{ActionRefiner, Result}

import scala.concurrent.Future

class MemberAction @Inject() (
  accountDao: AccountDao
) extends ActionRefiner[UserRequest, MemberRequest] {

  override protected def refine[A](
    request: UserRequest[A]
  ): Future[Either[Result, MemberRequest[A]]] = Future.successful {

    val accountOpt = for {
      sess <- request.userSession
      id <- sess.accountId
      a <- accountDao.find(id) if a.status == Account.Status.Active
    } yield a

    accountOpt.map { account =>
      Right(new MemberRequest(account, request))
    }.getOrElse {
      Left(Redirect(routes.AccountController.login()))
    }
  }
}
