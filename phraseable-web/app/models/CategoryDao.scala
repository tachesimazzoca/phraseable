package models

import javax.inject.{Inject, Singleton}

import anorm._
import components.util.Clock
import play.api.db.Database

@Singleton
class CategoryDao @Inject() (
  db: Database,
  clock: Clock
) extends AbstractDao[Category, Long](db) {

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

  private val selectByPhraseIdQuery = SQL(
    """
    SELECT a.* FROM category AS a, rel_phrase_category AS b
    WHERE a.id = b.category_id AND b.phrase_id = {phraseId}
    ORDER BY b.priority
    """
  )

  def selectByPhraseId(phraseId: Long): Seq[Category] =
    db.withConnection { implicit conn =>
      selectByPhraseIdQuery.on('phraseId -> phraseId).as(rowParser.*)
    }

  private val findByTitleQuery = SQL("SELECT * FROM category WHERE title = {title}")

  def findByTitle(title: String): Option[Category] =
    db.withConnection { implicit conn =>
      findByTitleQuery.on('title -> title).as(rowParser.singleOpt)
    }
}
