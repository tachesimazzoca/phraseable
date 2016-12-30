package models

import javax.inject.Inject

import anorm._
import components.util.Pagination
import play.api.db.Database

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

    case object PhraseCountDesc extends OrderBy("phrase_count DESC")

  }

}

class CategorySelectDao @Inject() (
  db: Database
) {

  import CategorySelectDao._

  private val countCategoriesQuery = SQL(
    """
       SELECT
         COUNT(*) AS c
       FROM
         category
       WHERE
         title LIKE {titleLike}
    """
  )

  private def selectCategoriesQuery(orderBy: String) = SQL(
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
       WHERE
         a.title LIKE {titleLike}
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

  def selectCategories(
    keyword: Option[String],
    offset: Long, limit: Long, orderBy: Option[OrderBy]
  ): Pagination[CategorySelect] = db.withConnection { implicit conn =>

    val titleLike = keyword.map(x => "${x}%").getOrElse("%")
    val orderByClause = orderBy.getOrElse(OrderBy.TitleAsc).clause

    Pagination.paginate(offset, limit,
      count = () =>
        countCategoriesQuery.on('titleLike -> titleLike).as(SqlParser.get[Long]("c").single),
      select = (theOffset, theLimit) => {
        selectCategoriesQuery(orderByClause).on(
          'titleLike -> titleLike,
          'offset -> theOffset,
          'limit -> theLimit
        ).as(rowParser.*)
      }
    )
  }
}
