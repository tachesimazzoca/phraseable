package models

import javax.inject.{Inject, Singleton}

import anorm._
import play.api.db.Database

@Singleton
class RelPhraseCategoryDao @Inject() (db: Database) {

  private val findByPhraseIdQuery =
    SQL(
      """
        SELECT category_id FROM rel_phrase_category WHERE phrase_id = {phrase_id}
      """)

  private val insertQuery =
    SQL(
      """
        INSERT INTO rel_phrase_category (phrase_id, category_id)
        VALUES ({phrase_id}, {category_id})
      """)

  private val deleteByPhraseIdQuery =
    SQL(
      """
         DELETE FROM rel_phrase_category WHERE phrase_id = {phrase_id}
      """)

  def findByPhraseId(phraseId: Long): Set[Long] =
    db.withConnection { implicit conn =>
      findByPhraseIdQuery.on('phrase_id -> phraseId).as(SqlParser.get[Long]("category_id").*).toSet
    }

  def updateByPhraseId(phraseId: Long, categoryIdSet: Set[Long]): Unit =
    db.withTransaction { implicit conn =>
      deleteByPhraseIdQuery.on('phrase_id -> phraseId).executeUpdate()
      categoryIdSet.foreach(x => insertQuery.on('phrase_id -> phraseId, 'category_id -> x).executeUpdate())
    }

  def deleteByPhraseId(phraseId: Long): Unit =
    db.withTransaction { implicit conn =>
      deleteByPhraseIdQuery.on('phrase_id -> phraseId).executeUpdate()
    }
}
