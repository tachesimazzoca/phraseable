package models

import components.util.Clock
import models.test._
import org.scalatest.FunSuite

class CategoryDaoSuite extends FunSuite {

  def createClock(t: Long) = new Clock {
    def currentTimeMillis = t
  }

  test("create / update / find") {
    withTestDatabase() { db =>
      val t = System.currentTimeMillis
      val categoryDao = new CategoryDao(db, createClock(t))

      val rows = Seq(
        Category(1L, "title", "desc")
      )
      rows.foreach(categoryDao.create(_))

      val date = Some(new java.util.Date(t))
      val inserted = Seq(
        Category(1L, "title", "desc", date, date)
      )

      assert(categoryDao.find(0L) === None)
      assert(categoryDao.find(1L) === Some(inserted(0)))
    }
  }

  test("selectByPhraseId") {
    withTestDatabase() { db =>
      val t = System.currentTimeMillis
      val categoryDao = new CategoryDao(db, createClock(t))
      val relPhraseCategoryDao = new RelPhraseCategoryDao(db)

      val rows = Seq(
        Category(1L, "cat1", "desc1"),
        Category(2L, "cat2", "desc2"),
        Category(3L, "cat3", "desc3")
      )
      rows.foreach(categoryDao.create(_))
      relPhraseCategoryDao.updateByPhraseId(1L, Seq(3L, 1L))

      assert(Seq("cat3", "cat1") === categoryDao.selectByPhraseId(1L).map(_.title),
        "CategoryDao#selectByPhraseId must returns rows in registered order")
    }
  }

  test("findByTitle") {
    withTestDatabase() { db =>
      val t = System.currentTimeMillis
      val categoryDao = new CategoryDao(db, createClock(t))

      val rows = Seq(
        Category(1L, "cat1", "desc1"),
        Category(2L, "cat2", "desc2")
      )
      rows.foreach(categoryDao.create(_))

      assert(2L === categoryDao.findByTitle("cat2").get.id)
    }
  }
}
