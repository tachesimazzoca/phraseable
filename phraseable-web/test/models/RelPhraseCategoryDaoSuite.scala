package models

import models.test._
import org.scalatest.FunSuite

class RelPhraseCategoryDaoSuite extends FunSuite {

  def createRelPhraseCategoryDao(): RelPhraseCategoryDao = new RelPhraseCategoryDao

  test("insert/find/delete") {
    withTestDatabase() { implicit database =>
      database.withConnection { implicit conn =>
        val relPhraseCategoryDao = createRelPhraseCategoryDao()
        relPhraseCategoryDao.updateByPhraseId(1L, Set(2L, 3L))
        assert(Set(2L, 3L) === relPhraseCategoryDao.findByPhraseId(1L))
        relPhraseCategoryDao.deleteByPhraseId(1L)
        assert(relPhraseCategoryDao.findByPhraseId(1L).isEmpty)
      }
    }
  }
}
