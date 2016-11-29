package models

import java.sql.SQLException

import components.util.{Clock, SystemClock}
import models.test._
import org.scalatest.FunSuite

class AccountDaoSuite extends FunSuite {

  def createClock(t: Long) = new Clock {
    def currentTimeMillis = t
  }

  test("username is unique") {
    withTestDatabase() { db =>
      val accountDao = new AccountDao(db, new SystemClock)
      val accounts = Seq(
        Account(1L, "alice1@example.net", Account.hashPassword("deadbeef"),
          Account.Status.Active),
        Account(2L, "alice1@example.net", Account.hashPassword("deadbeef"),
          Account.Status.Inactive)
      )
      accountDao.create(accounts(0))
      intercept[SQLException] {
        accountDao.create(accounts(1))
      }
    }
  }

  test("create / update / find") {
    withTestDatabase() { db =>
      val t = System.currentTimeMillis
      val accountDao = new AccountDao(db, createClock(t))
      val accounts = Seq(
        Account(1L, "alice@example.net", Account.hashPassword("deadbeef"),
          Account.Status.Active),
        Account(2L, "bob@example.net", Account.hashPassword("deadbeef"),
          Account.Status.Inactive)
      )
      accounts.foreach(accountDao.create(_))

      val inserted = Seq(
        Account(1L, "alice@example.net", accounts(0).password,
          Account.Status.Active,
          Some(new java.util.Date(t)), Some(new java.util.Date(t))),
        Account(2L, "bob@example.net", accounts(1).password,
          Account.Status.Inactive,
          Some(new java.util.Date(t)), Some(new java.util.Date(t)))
      )
      assert(accountDao.find(0L) === None)
      assert(accountDao.find(1L) === Some(inserted(0)))
      assert(accountDao.find(2L) === Some(inserted(1)))

      val updated = Seq(
        Account(1L, "alice1@example.net", accounts(0).password,
          Account.Status.Active,
          Some(new java.util.Date(t)), Some(new java.util.Date(t))),
        Account(2L, "bob@example.net", accounts(1).password,
          Account.Status.Inactive,
          Some(new java.util.Date(t)), Some(new java.util.Date(t)))
      )
      accountDao.update(inserted(0).copy(email = "alice1@example.net"))
      assert(accountDao.find(1L) === Some(updated(0)))
      assert(accountDao.find(2L) === Some(updated(1)))
    }
  }
}
