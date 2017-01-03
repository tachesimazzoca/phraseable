package models

import javax.inject.Inject

import anorm._
import components.util.Pagination
import play.api.db.Database

import scala.collection.mutable.ArrayBuffer

case class PhraseSelect(
  id: Long,
  term: String,
  translation: String,
  description: String
)

object PhraseSelectDao {

  sealed abstract class OrderBy(val clause: String)

  object OrderBy {

    case object IdAsc extends OrderBy("id ASC")

    case object TermAsc extends OrderBy("term ASC")

  }

  case class Condition(
    categoryIds: Seq[Long] = Seq.empty,
    keywords: Seq[String] = Seq.empty
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
      SqlParser.get[String]("term") ~
      SqlParser.get[String]("translation") ~
      SqlParser.get[String]("description") map {
      case id ~ term ~ translation ~ description =>
        PhraseSelect(id, term, translation, description)
    }
  }

  def selectByCondition(
    condition: Condition,
    offset: Long, limit: Long, orderBy: Option[OrderBy]
  ): Pagination[PhraseSelect] = db.withConnection { implicit conn =>

    val bindValues = new ArrayBuffer[NamedParameter]
    val whereConditions = new ArrayBuffer[String]

    // categoryId
    if (!condition.categoryIds.isEmpty) {
      whereConditions.append(
        """
        id IN (
          SELECT phrase_id
          FROM rel_phrase_category
          WHERE
            category_id IN ({categoryIds})
          GROUP BY phrase_id
        )
        """
      )
      bindValues.append('categoryIds -> condition.categoryIds)
    }
    // keywords
    if (!condition.keywords.isEmpty) {
      val pairs = new ArrayBuffer[String]
      condition.keywords.foldLeft(0) { (idx, x) =>
        val k = "termKeyword_%d".format(idx)
        pairs.append(s"term LIKE {${k}}")
        bindValues.append(Symbol(k) -> s"%${x}%")
        idx + 1
      }
      condition.keywords.foldLeft(0) { (idx, x) =>
        val k = "translationKeyword_%d".format(idx)
        pairs.append(s"translation LIKE {${k}}")
        bindValues.append(Symbol(k) -> s"%${x}%")
        idx + 1
      }
      whereConditions.append(pairs.mkString(" OR "))
    }

    val whereClause =
      if (whereConditions.isEmpty) ""
      else "WHERE " + whereConditions.mkString(" AND ")
    val orderByClause = orderBy.getOrElse(OrderBy.TermAsc).clause

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
