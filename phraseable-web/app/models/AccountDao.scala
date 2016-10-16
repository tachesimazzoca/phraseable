package models

import java.sql.Connection
import javax.inject.{Inject, Singleton}

import anorm._
import components.util.Clock

@Singleton
class AccountDao @Inject() (
  clock: Clock
) extends CRUDDaoSupport[Account, Long] {

  val tableName = "account"

  val idColumn = "id"

  val columns = Seq(
    "id", "username", "password_salt", "password_hash",
    "status", "email",
    "created_at", "updated_at"
  )

  val rowParser: RowParser[Account] = {
    import anorm.SqlParser._
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

  override def toNamedParameter(account: Account) = {
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

  override def onUpdate(account: Account): Account = {
    val t = clock.currentTimeMillis
    account.copy(
      createdAt = account.createdAt.orElse(Some(new java.util.Date(t))),
      updatedAt = Some(new java.util.Date(t))
    )
  }

  val findByUsernameQuery = SQL("SELECT * FROM account WHERE username = {username}")

  def findByUsername(username: String)(implicit conn: Connection): Option[Account] =
    findByUsernameQuery.on('username -> username).as(rowParser.singleOpt)
}
