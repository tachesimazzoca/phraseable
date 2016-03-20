package models

import models.test._
import org.scalatest.FunSuite
import play.api.db.Database

class IdSequenceDaoSuite extends FunSuite {

  def createIdSequenceDao()(implicit database: Database): IdSequenceDao =
    new IdSequenceDao(database)

  test("nextId(SequenceType.Account)") {
    withTestDatabase() { implicit database =>
      val idSequenceDao = createIdSequenceDao()
      val id1 = idSequenceDao.nextId(IdSequence.SequenceType.Account)
      val id2 = idSequenceDao.nextId(IdSequence.SequenceType.Account)
      assert(id1 === id2 - 1)
    }
  }
}
