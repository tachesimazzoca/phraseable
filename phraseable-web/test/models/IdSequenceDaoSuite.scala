package models

import models.test._
import org.scalatest.FunSuite

class IdSequenceDaoSuite extends FunSuite {

  def createIdSequenceDao(): IdSequenceDao = new IdSequenceDao

  test("nextId(SequenceType.Account)") {
    withTestDatabase() { implicit database =>
      database.withTransaction { implicit conn =>
        val idSequenceDao = createIdSequenceDao()
        val id1 = idSequenceDao.nextId(IdSequence.SequenceType.Account)
        val id2 = idSequenceDao.nextId(IdSequence.SequenceType.Account)
        assert(id1 === id2 - 1)
      }
    }
  }
}
