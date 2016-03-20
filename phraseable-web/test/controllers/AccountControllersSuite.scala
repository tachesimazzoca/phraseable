package controllers

import org.scalatest.FunSuite

import scala.concurrent.Future

import play.api.db.Database
import play.api.http._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import components.util.Timer
import models._
import models.test._

class AccountControllerSuite extends FunSuite {

  test("postEntry") {
    withTestDatabase() { database =>
      val accountDao = new AccountDao(database, Timer.systemTimer)
      val idSequenceDao = new IdSequenceDao(database)
      val accountController = new AccountController(accountDao, idSequenceDao)
      val postEntry = accountController.postEntry()

      assert(
        status(
          postEntry(
            FakeRequest().withFormUrlEncodedBody(
              "username" -> "",
              "password.main" -> "",
              "password.confirmation" -> ""
            )
          )
        ) === Status.BAD_REQUEST)

      assert(
        status(
          postEntry(
            FakeRequest().withFormUrlEncodedBody(
              "username" -> "alice",
              "password.main" -> "",
              "password.confirmation" -> ""
            )
          )
        ) === Status.BAD_REQUEST)

      assert(
        status(
          postEntry(
            FakeRequest().withFormUrlEncodedBody(
              "username" -> "alice",
              "password.main" -> "a",
              "password.confirmation" -> "a"
            )
          )
        ) === Status.BAD_REQUEST)

      assert(
        status(
          postEntry(
            FakeRequest().withFormUrlEncodedBody(
              "username" -> "alice",
              "password.main" -> "01234567",
              "password.confirmation" -> ""
            )
          )
        ) === Status.BAD_REQUEST)

      assert(
        status(
          postEntry(
            FakeRequest().withFormUrlEncodedBody(
              "username" -> "alice",
              "password.main" -> "01234567",
              "password.confirmation" -> "01234567"
            )
          )
        ) === Status.OK)

      assert(
        status(
          postEntry(
            FakeRequest().withFormUrlEncodedBody(
              "username" -> "alice",
              "password.main" -> "01234567",
              "password.confirmation" -> "01234567"
            )
          )
        ) === Status.BAD_REQUEST)

      assert(
        status(
          postEntry(
            FakeRequest().withFormUrlEncodedBody(
              "username" -> "bob",
              "password.main" -> "01234567",
              "password.confirmation" -> "01234567"
            )
          )
        ) === Status.OK)
    }
  }
}
