package models

import javax.inject.Inject

import anorm._
import components.util.Pagination
import play.api.db.Database

import scala.collection.mutable.ArrayBuffer

case class CategorySelect(
  id: Long,
  title: String,
  description: String,
  phraseCount: Long
)

object CategorySelectDao {

  sealed abstract class OrderBy(val clause: String)

  object OrderBy {

    case object IdAsc extends OrderBy("id ASC")

    case object TitleAsc extends OrderBy("title ASC")

    case object PhraseCountDesc extends OrderBy(
      "phrase_count DESC, title ASC, id ASC")

  }

  case class Condition(
    keywords: Seq[String] = Seq.empty
  )

}

class CategorySelectDao @Inject() (
  db: Database
) {

  import CategorySelectDao._

  private def countQuery(where: String) = SQL(
    s"""
    SELECT
      COUNT(*)
    FROM
      category AS a
    ${where}
    """
  )

  private def selectQuery(where: String, orderBy: String) = SQL(
    s"""
    SELECT
      a.id AS id,
      a.title AS title,
      a.description AS description,
      IFNULL(b.phrase_count, 0) AS phrase_count
    FROM
      category AS a
    LEFT JOIN (
      SELECT
        category_id,
        COUNT(phrase_id) AS phrase_count
      FROM rel_phrase_category
      GROUP BY category_id
    ) AS b
    ON b.category_id = a.id
    ${where}
    ORDER BY ${orderBy}
    LIMIT {offset}, {limit}
    """
  )

  private val rowParser: RowParser[CategorySelect] = {
    SqlParser.get[Long]("id") ~
      SqlParser.get[String]("title") ~
      SqlParser.get[String]("description") ~
      SqlParser.get[Long]("phrase_count") map {
      case id ~ title ~ description ~ phraseCount =>
        CategorySelect(id, title, description, phraseCount)
    }
  }

  def selectByCondition(
    condition: Condition,
    offset: Long, limit: Long, orderBy: Option[OrderBy]
  ): Pagination[CategorySelect] = db.withConnection { implicit conn =>

    val whereConditions = new ArrayBuffer[String]
    val bindValues = new ArrayBuffer[NamedParameter]

    // keywords
    if (!condition.keywords.isEmpty) {
      val pairs = new ArrayBuffer[String]
      condition.keywords.foldLeft(0) { (idx, x) =>
        val k = "title_%d".format(idx)
        pairs.append(s"title LIKE {${k}}")
        bindValues.append(Symbol(k) -> s"${x}%")
        idx + 1
      }
      whereConditions.append(pairs.mkString(" OR "))
    }

    val whereClause =
      if (whereConditions.isEmpty) ""
      else "WHERE " + whereConditions.mkString(" AND ")
    val orderByClause = orderBy.getOrElse(OrderBy.TitleAsc).clause

    Pagination.paginate(offset, limit,
      count = () =>
        countQuery(whereClause).on(bindValues: _*).as(SqlParser.get[Long](1).single),
      select = (theOffset, theLimit) => {
        val bindValuesWithLimit = bindValues ++ Seq[NamedParameter](
          'offset -> theOffset,
          'limit -> theLimit
        )
        selectQuery(whereClause, orderByClause).on(bindValuesWithLimit: _*).as(rowParser.*)
      }
    )
  }
}
