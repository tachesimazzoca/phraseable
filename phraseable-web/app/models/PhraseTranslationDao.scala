package models

import javax.inject.{Inject, Singleton}

import anorm._
import components.util.Clock

@Singleton
class PhraseTranslationDao @Inject() (
  clock: Clock
) extends CRUDDaoSupport[PhraseTranslation, Long] {

  val tableName = "phrase_translation"

  val idColumn = "id"

  val columns = Seq(
    "id", "phrase_id",
    "locale", "content",
    "created_at", "updated_at"
  )

  private def asDate(date: java.util.Date) = new java.util.Date(date.getTime)

  override val rowParser: RowParser[PhraseTranslation] = {
    SqlParser.get[Long]("id") ~
      SqlParser.get[Long]("phrase_id") ~
      SqlParser.get[String]("locale") ~
      SqlParser.get[String]("content") ~
      SqlParser.get[Option[java.util.Date]]("created_at") ~
      SqlParser.get[Option[java.util.Date]]("updated_at") map {
      case id ~ phraseId ~ locale ~ content ~ createdAt ~ updatedAt =>
        PhraseTranslation(id, phraseId, locale, content,
          createdAt.map(asDate),
          updatedAt.map(asDate))
    }
  }

  override def toNamedParameter(entity: PhraseTranslation) = {
    Seq[NamedParameter](
      'id -> entity.id,
      'phrase_id -> entity.phraseId,
      'locale -> entity.locale,
      'content -> entity.content,
      'created_at -> entity.createdAt,
      'updated_at -> entity.updatedAt
    )
  }

  override def onUpdate(entity: PhraseTranslation): PhraseTranslation = {
    val t = clock.currentTimeMillis
    entity.copy(
      createdAt = entity.createdAt.orElse(Some(new java.util.Date(t))),
      updatedAt = Some(new java.util.Date(t))
    )
  }
}
