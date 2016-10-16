package models

import java.sql.{Connection, SQLException}
import javax.inject.Singleton

import anorm._

@Singleton
class RelPhraseCategoryDao {

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

  def findByPhraseId(phraseId: Long)(implicit conn: Connection): Set[Long] = {
    findByPhraseIdQuery.on('phrase_id -> phraseId).fold(Set.empty[Long]) { (acc, row) =>
      acc + row[Long]("category_id")
    } match {
      case Right(xs) => xs
      case Left(es) => throw new SQLException(es.size + " errors found")
    }
  }

  def updateByPhraseId(phraseId: Long, categoryIdSet: Set[Long])(implicit conn: Connection): Unit = {
    deleteByPhraseIdQuery.on('phrase_id -> phraseId).executeUpdate()
    categoryIdSet.foreach(x => insertQuery.on('phrase_id -> phraseId, 'category_id -> x).executeUpdate())
  }

  def deleteByPhraseId(phraseId: Long)(implicit conn: Connection): Unit =
    deleteByPhraseIdQuery.on('phrase_id -> phraseId).executeUpdate()
}
