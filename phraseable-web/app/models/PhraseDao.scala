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
    "id", "lang", "content",
    "definition", "description",
    "created_at", "updated_at"
  )

  private def asDate(date: java.util.Date) = new java.util.Date(date.getTime)

  override val rowParser: RowParser[Phrase] = {
    SqlParser.get[Long]("id") ~
      SqlParser.get[String]("lang") ~
      SqlParser.get[String]("content") ~
      SqlParser.get[String]("definition") ~
      SqlParser.get[String]("description") ~
      SqlParser.get[Option[java.util.Date]]("created_at") ~
      SqlParser.get[Option[java.util.Date]]("updated_at") map {
      case id ~ language ~ content ~ definition ~ description ~ createdAt ~ updatedAt =>
        Phrase(id,
          Phrase.Lang.fromName(language),
          content,
          definition,
          description,
          createdAt.map(asDate),
          updatedAt.map(asDate))
    }
  }

  override def toNamedParameter(phrase: Phrase) = {
    Seq[NamedParameter](
      'id -> phrase.id,
      'lang -> phrase.lang.name,
      'content -> phrase.content,
      'definition -> phrase.definition,
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
