package models

import javax.inject.{Inject, Singleton}

import anorm._
import components.util.Clock
import play.api.db.Database

@Singleton
class PhraseDao @Inject() (
  db: Database,
  clock: Clock
) extends AbstractDao[Phrase, Long](db) {

  val tableName = "phrase"

  val idColumn = "id"

  val columns = Seq(
    "id", "content", "description", "created_at", "updated_at"
  )

  private def asDate(date: java.util.Date) = new java.util.Date(date.getTime)

  override val rowParser: RowParser[Phrase] = {
    SqlParser.get[Long]("id") ~
      SqlParser.get[String]("content") ~
      SqlParser.get[String]("description") ~
      SqlParser.get[Option[java.util.Date]]("created_at") ~
      SqlParser.get[Option[java.util.Date]]("updated_at") map {
      case id ~ content ~ description ~ createdAt ~ updatedAt =>
        Phrase(id, content, description,
          createdAt.map(asDate),
          updatedAt.map(asDate))
    }
  }

  override def toNamedParameter(phrase: Phrase) = {
    Seq[NamedParameter](
      'id -> phrase.id,
      'content -> phrase.content,
      'description -> phrase.description,
      'created_at -> phrase.createdAt,
      'updated_at -> phrase.updatedAt
    )
  }

  override def onUpdate(phrase: Phrase): Phrase = {
    val t = clock.currentTimeMillis
    phrase.copy(
      createdAt = phrase.createdAt.orElse(Some(new java.util.Date(t))),
      updatedAt = Some(new java.util.Date(t))
    )
  }
}
