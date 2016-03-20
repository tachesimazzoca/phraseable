package models

import javax.inject.{Inject, Singleton}

import anorm._
import play.api.db.{Database, NamedDatabase}

import components.util.Timer

@Singleton
class AccountDao @Inject() (
  db: Database,
  timer: Timer
) {
  import AccountDao._

  def find(id: Long): Option[Account] =
    db.withConnection { implicit conn =>
      findQuery.on('id -> id).as(parser.singleOpt)
    }

  def create(account: Account): Account =
    db.withTransaction { implicit conn =>
      val t = timer.currentTimeMillis
      val created = account.copy(
        createdAt = Some(new java.util.Date(t)),
        updatedAt = None
      )
      insertQuery.on(toNamedParameter(created): _*).executeUpdate()
      created
    }

  def update(account: Account): Account =
    db.withTransaction { implicit conn =>
      val t = timer.currentTimeMillis
      val updated = account.copy(
        createdAt = account.createdAt.orElse(Some(new java.util.Date(t))),
        updatedAt = Some(new java.util.Date(t))
      )
      updateQuery.on(toNamedParameter(updated): _*).executeUpdate()
      updated
    }

  private def toNamedParameter(account: Account) = {
    Seq[NamedParameter](
      'id -> account.id,
      'username -> account.username,
      'password_salt -> account.password.salt,
      'password_hash -> account.password.hash,
      'status -> account.status.value,
      'email -> account.email,
      'created_at -> account.createdAt,
      'updated_at -> account.updatedAt
    )
  }
}

object AccountDao {
  import anorm.SqlParser._

  def parser: RowParser[Account] = {
    get[Long]("id") ~
    get[String]("username") ~
    get[String]("password_salt") ~
    get[String]("password_hash") ~
    get[Int]("status") ~
    get[String]("email") ~
    get[Option[java.util.Date]]("created_at") ~
    get[Option[java.util.Date]]("updated_at") map {
      case id ~ username ~ passwordSalt ~ passwordHash ~
        status ~ email ~ createdAt ~ updatedAt =>
        Account(id, username,
          Account.Password(passwordSalt, passwordHash),
          Account.Status.fromValue(status), email,
          createdAt.map { ts => new java.util.Date(ts.getTime) },
          updatedAt.map { ts => new java.util.Date(ts.getTime) })
    }
  }

  val findQuery = SQL("SELECT * FROM accounts WHERE id = {id}")

  val insertQuery = SQL(
    """
    INSERT INTO accounts (id, username,
      password_salt, password_hash, status, email,
      created_at, updated_at)
    VALUES ({id}, {username},
      {password_salt}, {password_hash}, {status}, {email},
      {created_at}, {updated_at})
    """
  )

  val updateQuery = SQL(
    """
    UPDATE accounts SET username = {username},
      password_salt = {password_salt},
      password_hash = {password_hash},
      status = {status},
      email = {email},
      created_at = {created_at},
      updated_at = {updated_at}
    WHERE id = {id}
    """
  )
}
