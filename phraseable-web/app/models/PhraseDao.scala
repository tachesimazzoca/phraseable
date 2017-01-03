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
    "id", "lang", "term",
    "translation", "description",
    "created_at", "updated_at"
  )

  private def asDate(date: java.util.Date) = new java.util.Date(date.getTime)

  override val rowParser: RowParser[Phrase] = {
    SqlParser.get[Long]("id") ~
      SqlParser.get[String]("lang") ~
      SqlParser.get[String]("term") ~
      SqlParser.get[String]("translation") ~
      SqlParser.get[String]("description") ~
      SqlParser.get[Option[java.util.Date]]("created_at") ~
      SqlParser.get[Option[java.util.Date]]("updated_at") map {
      case id ~ language ~ term ~ translation ~ description ~ createdAt ~ updatedAt =>
        Phrase(id,
          Phrase.Lang.fromName(language),
          term,
          translation,
          description,
          createdAt.map(asDate),
          updatedAt.map(asDate))
    }
  }

  override def toNamedParameter(phrase: Phrase) = {
    Seq[NamedParameter](
      'id -> phrase.id,
      'lang -> phrase.lang.name,
      'term -> phrase.term,
      'translation -> phrase.translation,
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
