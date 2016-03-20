package models

import java.sql.SQLException

import anorm._
import org.scalatest.FunSuite
import play.api.db.Database

import models.test._
import components.util.Timer

class AccountDaoSuite extends FunSuite {
  def createAccountDao(
    timer: Timer = Timer.systemTimer
  )(implicit database: Database): AccountDao =
      new AccountDao(database, timer)

  def createTimer(t: Long) = new Timer {
    def currentTimeMillis = t
  }

  test("username is unique") {
    withTestDatabase() { implicit database =>
      val accountDao = createAccountDao()
      val accounts = Seq(
        Account(1L, "alice", Account.hashPassword("deadbeef"),
          Account.Status.Active, "alice1@example.net"),
        Account(2L, "alice", Account.hashPassword("deadbeef"),
          Account.Status.Inactive, "alice2@example.net")
      )
      accountDao.create(accounts(0))
      intercept[SQLException] {
        accountDao.create(accounts(1))
      }
    }
  }

  test("email is unique") {
    withTestDatabase() { implicit database =>
      val accountDao = createAccountDao()
      val accounts = Seq(
        Account(1L, "alice1", Account.hashPassword("deadbeef"),
          Account.Status.Active, "alice@example.net"),
        Account(2L, "alice2", Account.hashPassword("deadbeef"),
          Account.Status.Inactive, "alice@example.net")
      )
      accountDao.create(accounts(0))
      intercept[SQLException] {
        accountDao.create(accounts(1))
      }
    }
  }

  test("create / update / find") {
    withTestDatabase() { implicit database =>
      val t = System.currentTimeMillis
      val accountDao = createAccountDao(createTimer(t))
      val accounts = Seq(
        Account(1L, "alice", Account.hashPassword("deadbeef"),
          Account.Status.Active, "alice@example.net"),
        Account(2L, "bob", Account.hashPassword("deadbeef"),
          Account.Status.Inactive, "bob@example.net")
      )
      accounts.foreach(accountDao.create(_))

      val inserted = Seq(
        Account(1L, "alice", accounts(0).password,
          Account.Status.Active, "alice@example.net",
          Some(new java.util.Date(t)), None),
        Account(2L, "bob", accounts(1).password,
          Account.Status.Inactive, "bob@example.net",
          Some(new java.util.Date(t)), None)
      )
      assert(accountDao.find(0L) === None)
      assert(accountDao.find(1L) === Some(inserted(0)))
      assert(accountDao.find(2L) === Some(inserted(1)))

      val updated = Seq(
        Account(1L, "alice1", accounts(0).password,
          Account.Status.Active, "alice@example.net",
          Some(new java.util.Date(t)), Some(new java.util.Date(t))),
        Account(2L, "bob", accounts(1).password,
          Account.Status.Inactive, "bob@example.net",
          Some(new java.util.Date(t)), None)
      )
      accountDao.update(inserted(0).copy(username = "alice1"))
      assert(accountDao.find(1L) === Some(updated(0)))
      assert(accountDao.find(2L) === Some(updated(1)))
    }
  }
}
