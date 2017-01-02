package models

import javax.inject.{Inject, Singleton}

import anorm._
import play.api.db.Database

@Singleton
class RelPhraseCategoryDao @Inject() (db: Database) {

  private val findByPhraseIdQuery =
    SQL(
      """
        |SELECT category_id FROM rel_phrase_category
        | WHERE phrase_id = {phrase_id} ORDER BY priority
      """.stripMargin)

  private val insertQuery =
    SQL(
      """
        |INSERT INTO rel_phrase_category (phrase_id, category_id, priority)
        | VALUES ({phrase_id}, {category_id}, {priority})
      """.stripMargin)

  private val deleteByPhraseIdQuery =
    SQL("DELETE FROM rel_phrase_category WHERE phrase_id = {phrase_id}")

  def findByPhraseId(phraseId: Long): Seq[Long] =
    db.withConnection { implicit conn =>
      findByPhraseIdQuery.on('phrase_id -> phraseId).as(SqlParser.get[Long]("category_id").*)
    }

  def updateByPhraseId(phraseId: Long, categoryIdSeq: Seq[Long]): Unit =
    db.withTransaction { implicit conn =>
      deleteByPhraseIdQuery.on('phrase_id -> phraseId).executeUpdate()
      categoryIdSeq.foldLeft(1) { (priority, categoryId) =>
        insertQuery.on('phrase_id -> phraseId, 'category_id -> categoryId,
          'priority -> priority).executeUpdate()
        priority + 1
      }
    }

  def deleteByPhraseId(phraseId: Long): Unit =
    db.withTransaction { implicit conn =>
      deleteByPhraseIdQuery.on('phrase_id -> phraseId).executeUpdate()
    }

  private lazy val truncateQuery =
    SQL("TRUNCATE TABLE rel_phrase_category")

  def truncate(): Unit =
    db.withConnection { implicit conn =>
      truncateQuery.execute()
    }
}
