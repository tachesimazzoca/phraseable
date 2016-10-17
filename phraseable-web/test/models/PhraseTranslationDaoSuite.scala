package models

import components.util.Clock
import models.test._
import org.scalatest.FunSuite

class PhraseTranslationDaoSuite extends FunSuite {

  def createClock(t: Long) = new Clock {
    def currentTimeMillis = t
  }

  test("create / update / find") {
    withTestDatabase() { db =>
      val t = System.currentTimeMillis
      val phraseTranslationDao = new PhraseTranslationDao(db, createClock(t))

      val rows = Seq(
        PhraseTranslation(1L, 2L, "ja_JP", "translation")
      )
      rows.foreach(phraseTranslationDao.create(_))

      val date = Some(new java.util.Date(t))
      val inserted = Seq(
        PhraseTranslation(1L, 2L, "ja_JP", "translation", date, date)
      )

      assert(phraseTranslationDao.find(0L) === None)
      assert(phraseTranslationDao.find(1L) === Some(inserted(0)))
    }
  }
}
