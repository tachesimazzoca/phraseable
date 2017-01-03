package models

import javax.inject.{Inject, Singleton}

import anorm._
import play.api.db.Database

@Singleton
class PhraseKeywordDao @Inject() (db: Database) {

  private val insertQuery =
    SQL(
      """
        |INSERT INTO phrase_keyword (phrase_id, keyword)
        | VALUES ({phrase_id}, {keyword})
      """.stripMargin)

  private val deleteByPhraseIdQuery =
    SQL("DELETE FROM phrase_keyword WHERE phrase_id = {phrase_id}")

  def updateKeywords(phraseId: Long, keywords: Seq[String]): Unit =
    db.withTransaction { implicit conn =>
      deleteByPhraseIdQuery.on('phrase_id -> phraseId).execute()
      for (x <- keywords) {
        insertQuery.on('phrase_id -> phraseId, 'keyword -> x).execute()
      }
    }

  private lazy val truncateQuery =
    SQL("TRUNCATE TABLE phrase_keyword")

  def truncate(): Unit =
    db.withConnection { implicit conn =>
      truncateQuery.execute()
    }
}

