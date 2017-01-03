package models

import components.util.SystemClock
import models.test._
import org.scalatest.FunSuite

class PhraseSelectDaoSuite extends FunSuite {
  test("selectByCondition") {
    withTestDatabase() { implicit db =>
      val categoryDao = new CategoryDao(db, new SystemClock())
      val phraseDao = new PhraseDao(db, new SystemClock())
      val relPhraseCategoryDao = new RelPhraseCategoryDao(db)

      val phraseSelectDao = new PhraseSelectDao(db)
      val pagination = phraseSelectDao.selectByCondition(
        PhraseSelectDao.Condition(), 0, 20, None)
      assert(Seq.empty[PhraseSelect] === pagination.rows)

      Seq(
        Category(1L, "cat1", ""),
        Category(2L, "cat2", ""),
        Category(3L, "cat3", ""),
        Category(4L, "cat4", "")
      ).foreach(categoryDao.create)

      Seq(
        Phrase(1L, Phrase.Lang.English, "buy [something]", "def1", "desc1"),
        Phrase(2L, Phrase.Lang.English, "feel free to [do]", "def2", "desc2"),
        Phrase(3L, Phrase.Lang.English, "approve of [A]", "def3", "desc3"),
        Phrase(4L, Phrase.Lang.English, "bring [A] to [B]", "def4", "desc4")
      ).foreach(phraseDao.create)

      Seq(
        (1L, Seq(1L, 2L)),
        (2L, Seq(2L, 3L)),
        (3L, Seq(1L, 3L, 4L)),
        (4L, Seq(4L))
      ).foreach(x => relPhraseCategoryDao.updateByPhraseId(x._1, x._2))

      val orderById = phraseSelectDao.selectByCondition(
        PhraseSelectDao.Condition(),
        0, 10, Some(PhraseSelectDao.OrderBy.IdAsc))
      assert(Seq(1L, 2L, 3L, 4L) === orderById.rows.map(_.id))

      val orderByTerm = phraseSelectDao.selectByCondition(
        PhraseSelectDao.Condition(),
        0, 10, Some(PhraseSelectDao.OrderBy.TermAsc))
      assert(Seq(3L, 4L, 1L, 2L) === orderByTerm.rows.map(_.id))

      val filteredByCategoryIds = phraseSelectDao.selectByCondition(
        PhraseSelectDao.Condition(Seq(3L)),
        0, 10, Some(PhraseSelectDao.OrderBy.TermAsc))
      assert(Seq(3L, 2L) === filteredByCategoryIds.rows.map(_.id))
    }
  }
}
