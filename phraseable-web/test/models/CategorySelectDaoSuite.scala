package models

import components.util.SystemClock
import models.test._
import org.scalatest.FunSuite

class CategorySelectDaoSuite extends FunSuite {
  test("selectByCondition") {
    withTestDatabase() { implicit db =>
      val categoryDao = new CategoryDao(db, new SystemClock())
      val relPhraseCategoryDao = new RelPhraseCategoryDao(db)

      val categorySelectDao = new CategorySelectDao(db)
      val pagination = categorySelectDao.selectByCondition(
        CategorySelectDao.Condition(), 0, 20, None)
      assert(Seq.empty[PhraseSelect] === pagination.rows)

      Seq(
        Category(1L, "Concrete Nouns", ""),
        Category(2L, "Adverbs", ""),
        Category(3L, "Transive Verbs", ""),
        Category(4L, "Adverbial Conjunction", "")
      ).foreach(categoryDao.create)

      Seq(
        (1L, Seq(2L, 3L)),
        (2L, Seq(1L, 3L)),
        (3L, Seq(2L, 4L)),
        (4L, Seq(2L))
      ).foreach(x => relPhraseCategoryDao.updateByPhraseId(x._1, x._2))

      val orderById = categorySelectDao.selectByCondition(
        CategorySelectDao.Condition(),
        0, 10, Some(CategorySelectDao.OrderBy.IdAsc))
      assert(Seq(1L, 2L, 3L, 4L) === orderById.rows.map(_.id))

      val orderByTitle = categorySelectDao.selectByCondition(
        CategorySelectDao.Condition(),
        0, 10, Some(CategorySelectDao.OrderBy.TitleAsc))
      assert(Seq(4L, 2L, 1L, 3L) === orderByTitle.rows.map(_.id))

      val orderByPhraseCount = categorySelectDao.selectByCondition(
        CategorySelectDao.Condition(),
        0, 10, Some(CategorySelectDao.OrderBy.PhraseCountDesc))
      assert(Seq(2L, 3L, 4L, 1L) === orderByPhraseCount.rows.map(_.id))

      val filteredByTitle = categorySelectDao.selectByCondition(
        CategorySelectDao.Condition(Seq("Ad", "Con")),
        0, 10, Some(CategorySelectDao.OrderBy.TitleAsc))
      assert(Seq(4L, 2L, 1L) === filteredByTitle.rows.map(_.id))
    }
  }
}
