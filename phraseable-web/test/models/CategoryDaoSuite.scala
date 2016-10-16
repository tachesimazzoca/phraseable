package models

import components.util.{Clock, SystemClock}
import models.test._
import org.scalatest.FunSuite

class CategoryDaoSuite extends FunSuite {
  def createCategoryDao(clock: Clock = new SystemClock): CategoryDao = new CategoryDao(clock)

  def createClock(t: Long) = new Clock {
    def currentTimeMillis = t
  }

  test("create / update / find") {
    withTestDatabase() { database =>
      database.withTransaction { implicit conn =>
        val t = System.currentTimeMillis
        val categoryDao = createCategoryDao(createClock(t))

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
}
