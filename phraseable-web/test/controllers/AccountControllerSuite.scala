package controllers

import components.util.SystemClock
import models._
import models.test._
import org.scalatest.FunSuite
import play.api.http._
import play.api.test.Helpers._
import play.api.test._

class AccountControllerSuite extends FunSuite {

  val systemClock = new SystemClock

  test("postEntry") {
    withTestDatabase() { db =>
      val accountDao = new AccountDao(db, systemClock)
      val idSequenceDao = new IdSequenceDao(db)
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
