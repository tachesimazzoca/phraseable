package models

import anorm._
import models.test._
import org.scalatest.FunSuite
import play.api.db.Database

class AccountDaoSuite extends FunSuite {

  private val insertQuery = SQL(
    "INSERT INTO accounts (id, username) VALUES ({id}, {username})"
  )

  private val truncateQuery = SQL("TRUNCATE TABLE accounts")

  private def withAccountsTable[T](
    accounts: Seq[Account] = Seq()
  )(block: Database => T): T = {
    withTestDatabase() { database =>
      implicit val conn = database.getConnection
      accounts.foreach { x =>
        val params = Seq[NamedParameter](
          'id -> x.id,
          'username -> x.username)
        insertQuery.on(params: _*).executeUpdate()
      }
      val ret = block(database)
      truncateQuery.executeUpdate()
      ret
    }
  }

  test("find") {
    val accounts = Seq(
      Account(1L, "alice")
    )
    withAccountsTable(accounts) { database =>
      val accountDao = new AccountDao(database)
      assert(accountDao.find(0L) === None)
      assert(accountDao.find(1L) === Some(accounts(0)))
    }
  }
}
