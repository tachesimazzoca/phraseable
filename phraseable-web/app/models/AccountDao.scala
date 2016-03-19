package models

import javax.inject.{Inject, Singleton}

import anorm._
import play.api.db.{Database, NamedDatabase}

@Singleton
class AccountDao @Inject() (
  @NamedDatabase("default") db: Database
) {
  import AccountDao._

  def find(id: Long): Option[Account] = db.withConnection { implicit conn =>
    findQuery.on('id -> id).as(parser.singleOpt)
  }
}

object AccountDao {
  def parser: RowParser[Account] = {
    SqlParser.long("id") ~
    SqlParser.str("username") map {
      case id ~ username => Account(id, username)
    }
  }

  val findQuery = SQL("SELECT * FROM accounts WHERE id = {id}")
}
