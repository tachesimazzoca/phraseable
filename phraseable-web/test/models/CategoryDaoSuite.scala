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
}
