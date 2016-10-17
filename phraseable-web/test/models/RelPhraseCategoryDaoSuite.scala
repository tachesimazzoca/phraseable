package models

import models.test._
import org.scalatest.FunSuite

class RelPhraseCategoryDaoSuite extends FunSuite {

  test("insert/find/delete") {
    withTestDatabase() { implicit db =>
      val relPhraseCategoryDao = new RelPhraseCategoryDao(db)
      relPhraseCategoryDao.updateByPhraseId(1L, Set(2L, 3L))
      assert(Set(2L, 3L) === relPhraseCategoryDao.findByPhraseId(1L))
      relPhraseCategoryDao.deleteByPhraseId(1L)
      assert(relPhraseCategoryDao.findByPhraseId(1L).isEmpty)
    }
  }
}
