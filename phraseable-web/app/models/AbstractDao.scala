package models

import anorm._
import play.api.db.Database

abstract class AbstractDao[T, U](db: Database) {

  val tableName: String

  val idColumn: String

  val columns: Seq[String]

  val rowParser: RowParser[T]

  def toNamedParameter(entity: T): Seq[NamedParameter]

  def onUpdate(entity: T): T = identity(entity)

  private lazy val findQuery: SqlQuery =
    SQL(s"""SELECT * FROM ${tableName} WHERE ${idColumn} = {${idColumn}}""")

  private lazy val insertQuery: SqlQuery = SQL(
    s"INSERT INTO ${tableName} (" + columns.mkString(", ") + ") VALUES (" +
      columns.map(x => s"{${x}}").mkString(", ") + ")")

  private lazy val updateQuery: SqlQuery = SQL(
    s"UPDATE ${tableName} SET " + columns.map(x => s"${x} = {${x}}").mkString(", ") +
      s" WHERE ${idColumn} = {${idColumn}}")

  private lazy val deleteQuery: SqlQuery =
    SQL(s"""DELETE FROM ${tableName} WHERE ${idColumn} = {${idColumn}}""")

  private def toIdValue(id: U): ParameterValue =
    if (id.isInstanceOf[Int]) id.asInstanceOf[Int]
    else if (id.isInstanceOf[Long]) id.asInstanceOf[Long]
    else if (id.isInstanceOf[String]) id.asInstanceOf[String]
    else throw new IllegalArgumentException("The type of id value must be Int/Long/String")

  def find(id: U): Option[T] =
    db.withConnection { implicit conn =>
      findQuery.on(Symbol(idColumn) -> toIdValue(id)).as(rowParser.singleOpt)
    }

  def create(entity: T): T =
    db.withTransaction { implicit conn =>
      insertQuery.on(toNamedParameter(onUpdate(entity)): _*).executeUpdate()
      entity
    }

  def update(entity: T): T =
    db.withTransaction { implicit conn =>
      updateQuery.on(toNamedParameter(onUpdate(entity)): _*).executeUpdate()
      entity
    }

  def delete(id: U): Unit =
    db.withTransaction { implicit conn =>
      deleteQuery.on(Symbol(idColumn) -> toIdValue(id)).executeUpdate()
    }
}
