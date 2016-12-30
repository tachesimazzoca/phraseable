package models

import javax.inject.{Inject, Singleton}

import anorm._
import play.api.db.Database

@Singleton
class RelPhraseCategoryDao @Inject() (db: Database) {

  private val findByPhraseIdQuery =
    SQL(
      """
        SELECT category_id FROM rel_phrase_category
        WHERE phrase_id = {phrase_id} ORDER BY priority
      """)

  private val insertQuery =
    SQL(
      """
        INSERT INTO rel_phrase_category (phrase_id, category_id, priority)
        VALUES ({phrase_id}, {category_id}, {priority})
      """)

  private val deleteByPhraseIdQuery =
    SQL(
      """
         DELETE FROM rel_phrase_category WHERE phrase_id = {phrase_id}
      """)

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
}
