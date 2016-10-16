package models

import components.util.{Clock, SystemClock}
import models.test._
import org.scalatest.FunSuite

class PhraseDaoSuite extends FunSuite {
  def createPhraseDao(clock: Clock = new SystemClock): PhraseDao = new PhraseDao(clock)

  def createClock(t: Long) = new Clock {
    def currentTimeMillis = t
  }

  test("create / update / find") {
    withTestDatabase() { database =>
      database.withTransaction { implicit conn =>
        val t = System.currentTimeMillis
        val phraseDao = createPhraseDao(createClock(t))

        val rows = Seq(
          Phrase(1L, "go to <A>", "desc")
        )
        rows.foreach(phraseDao.create(_))

        val date = Some(new java.util.Date(t))
        val inserted = Seq(
          Phrase(1L, "go to <A>", "desc", date, date)
        )

        assert(phraseDao.find(0L) === None)
        assert(phraseDao.find(1L) === Some(inserted(0)))
      }
    }
  }
}
