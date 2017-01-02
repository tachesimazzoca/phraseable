package models

import javax.inject.{Inject, Singleton}

import anorm.SqlParser._
import anorm._
import play.api.db.Database

@Singleton
class IdSequenceDao @Inject() (db: Database) {

  import models.IdSequence._

  private val selectForUpdateQuery =
    SQL(
      """
        |SELECT sequence_value FROM id_sequence
        | WHERE sequence_name = {sequence_name} FOR UPDATE
      """.stripMargin)

  private val updateQuery =
    SQL(
      """
        |UPDATE id_sequence SET sequence_value = {sequence_value}
        | WHERE sequence_name = {sequence_name}
      """.stripMargin)

  def nextId(sequenceType: SequenceType): Long =
    db.withTransaction { implicit conn =>
      val currentId = selectForUpdateQuery.on(
        'sequence_name -> sequenceType.name
      ).as(get[Long]("sequence_value").single)
      val nextId = sequenceType.assigner(currentId)
      updateQuery.on(
        'sequence_name -> sequenceType.name,
        'sequence_value -> nextId
      ).executeUpdate()
      nextId
    }

  def reset(sequenceType: SequenceType): Unit =
    db.withTransaction { implicit conn =>
      updateQuery.on(
        'sequence_name -> sequenceType.name,
        'sequence_value -> 0
      ).executeUpdate()
    }
}
