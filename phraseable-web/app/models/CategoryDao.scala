package models

import javax.inject.{Inject, Singleton}

import anorm._
import components.util.Clock

@Singleton
class CategoryDao @Inject() (
  clock: Clock
) extends CRUDDaoSupport[Category, Long] {

  val tableName = "category"

  val idColumn = "id"

  val columns = Seq(
    "id", "title", "description",
    "created_at", "updated_at"
  )

  private def asDate(date: java.util.Date) = new java.util.Date(date.getTime)

  override val rowParser: RowParser[Category] = {
    SqlParser.get[Long]("id") ~
      SqlParser.get[String]("title") ~
      SqlParser.get[String]("description") ~
      SqlParser.get[Option[java.util.Date]]("created_at") ~
      SqlParser.get[Option[java.util.Date]]("updated_at") map {
      case id ~ title ~ description ~ createdAt ~ updatedAt =>
        Category(id, title, description, createdAt.map(asDate), updatedAt.map(asDate))
    }
  }

  override def toNamedParameter(entity: Category) = {
    Seq[NamedParameter](
      'id -> entity.id,
      'title -> entity.title,
      'description -> entity.description,
      'created_at -> entity.createdAt,
      'updated_at -> entity.updatedAt
    )
  }

  override def onUpdate(entity: Category): Category = {
    val t = clock.currentTimeMillis
    entity.copy(
      createdAt = entity.createdAt.orElse(Some(new java.util.Date(t))),
      updatedAt = Some(new java.util.Date(t))
    )
  }
}
