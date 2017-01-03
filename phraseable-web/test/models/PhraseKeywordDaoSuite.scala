package models

import anorm._
import models.test._
import org.scalatest.FunSuite

class PhraseKeywordDaoSuite extends FunSuite {

  test("updateKeywords") {
    withTestDatabase() { implicit db =>
      val phraseKeywordDao = new PhraseKeywordDao(db)

      val selectByPhraseIdQuery = SQL(
        """SELECT keyword FROM phrase_keyword WHERE phrase_id = {phrase_id}"""
      )

      phraseKeywordDao.updateKeywords(1L, Seq("foo", "bar", "baz"))
      db.withConnection { implicit conn =>
        assert(Seq("foo", "bar", "baz") === selectByPhraseIdQuery
          .on('phrase_id -> 1).as(SqlParser.str("keyword").*))
      }

      phraseKeywordDao.updateKeywords(2L, Seq("foo", "baz"))
      db.withConnection { implicit conn =>
        assert(Seq("foo", "bar", "baz") === selectByPhraseIdQuery
          .on('phrase_id -> 1).as(SqlParser.str("keyword").*))
        assert(Seq("foo", "baz") === selectByPhraseIdQuery
          .on('phrase_id -> 2).as(SqlParser.str("keyword").*))
      }

      phraseKeywordDao.updateKeywords(2L, Seq.empty)
      db.withConnection { implicit conn =>
        assert(Seq("foo", "bar", "baz") === selectByPhraseIdQuery
          .on('phrase_id -> 1).as(SqlParser.str("keyword").*))
        assert(Seq.empty[String] === selectByPhraseIdQuery
          .on('phrase_id -> 2).as(SqlParser.str("keyword").*))
      }
    }
  }
}
