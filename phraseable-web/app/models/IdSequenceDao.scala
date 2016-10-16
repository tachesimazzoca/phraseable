package models

import java.sql.Connection
import javax.inject.Singleton

import anorm.SqlParser._
import anorm._

@Singleton
class IdSequenceDao {

  import models.IdSequence._

  private val selectForUpdateQuery =
    SQL(
      """
        SELECT sequence_value FROM id_sequence
        WHERE sequence_name = {sequence_name} FOR UPDATE
      """)

  private val updateQuery =
    SQL(
      """
        UPDATE id_sequence SET sequence_value = {sequence_value}
        WHERE sequence_name = {sequence_name}
      """)

  def nextId(sequenceType: SequenceType)(implicit conn: Connection): Long = {
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
}
