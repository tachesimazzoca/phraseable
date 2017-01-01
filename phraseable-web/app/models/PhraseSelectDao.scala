package models

import javax.inject.Inject

import anorm._
import components.util.Pagination
import play.api.db.Database

import scala.collection.mutable.ArrayBuffer

case class PhraseSelect(
  id: Long,
  content: String,
  definition: String,
  description: String
)

object PhraseSelectDao {

  sealed abstract class OrderBy(val clause: String)

  object OrderBy {

    case object IdAsc extends OrderBy("id ASC")

    case object ContentAsc extends OrderBy("content ASC")

  }

  case class Condition(
    categoryId: Option[Long] = None,
    categoryTitles: Seq[String] = Seq.empty
  )
}

class PhraseSelectDao @Inject() (
  db: Database
) {

  import PhraseSelectDao._

  private def countQuery(where: String) = SQL(
    s"""
    SELECT COUNT(*)
    FROM phrase
    ${where}
    """
  )

  private def selectQuery(where: String, orderBy: String) = SQL(
    s"""
    SELECT *
    FROM phrase
    ${where}
    ORDER BY ${orderBy}
    LIMIT {offset}, {limit}
    """
  )

  private val rowParser: RowParser[PhraseSelect] = {
    SqlParser.get[Long]("id") ~
      SqlParser.get[String]("content") ~
      SqlParser.get[String]("definition") ~
      SqlParser.get[String]("description") map {
      case id ~ content ~ definition ~ description =>
        PhraseSelect(id, content, definition, description)
    }
  }

  def selectByCondition(
    condition: Condition,
    offset: Long, limit: Long, orderBy: Option[OrderBy]
  ): Pagination[PhraseSelect] = db.withConnection { implicit conn =>

    val bindValues = new ArrayBuffer[NamedParameter]
    val whereConditions = new ArrayBuffer[String]
    if (condition.categoryId.isDefined) {
      whereConditions.append(
        """
        id IN (
          SELECT phrase_id
          FROM rel_phrase_category
          WHERE
            category_id = {categoryId}
          GROUP BY phrase_id
        )
        """
      )
      bindValues.append('categoryId -> condition.categoryId.get)
    }
    if (!condition.categoryTitles.isEmpty) {
      whereConditions.append(
        """
        id IN (
          SELECT a.phrase_id
          FROM rel_phrase_category AS a, category AS b
          WHERE
            b.id = a.category_id
            AND b.title IN ({categoryTitles})
          GROUP BY a.phrase_id
        )
        """
      )
      bindValues.append('categoryTitles -> condition.categoryTitles)
    }
    val whereClause = if (whereConditions.isEmpty) ""
    else {
      "WHERE " + whereConditions.mkString(" AND ")
    }
    val orderByClause = orderBy.getOrElse(OrderBy.ContentAsc).clause
    Pagination.paginate(offset, limit,
      count = () =>
        countQuery(whereClause).on(bindValues: _*).as(SqlParser.get[Long](1).single),
      select = (theOffset, theLimit) => {
        val bindValuesWithLimit =
          bindValues ++ Seq[NamedParameter](
            'offset -> theOffset,
            'limit -> theLimit
          )
        selectQuery(whereClause, orderByClause).on(bindValuesWithLimit: _*).as(rowParser.*)
      }
    )
  }
}
