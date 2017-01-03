package models.form

import org.apache.commons.lang3.StringUtils
import play.api.data.Forms._
import play.api.data._

import scala.collection.mutable.ArrayBuffer

case class KeywordSearchForm(
  keywords: Seq[String] = Seq.empty,
  offset: Option[Long] = None,
  limit: Option[Long] = None,
  orderBy: Option[String] = None
)

object KeywordSearchForm extends NormalizationSupport {

  private val form = Form(
    mapping(
      "q" -> optional(text),
      "offset" -> optional(longNumber),
      "limit" -> optional(longNumber),
      "order" -> optional(text)
    ) { (q, offset, limit, order) =>
      val keywords = q.map(parseSearchQuery).getOrElse(Seq.empty)
      KeywordSearchForm(keywords, offset, limit, order)
    } { a =>
      val q = if (a.keywords.isEmpty) None else Some(convertToSearchQuery(a.keywords))
      Some(q, a.offset, a.limit, a.orderBy)
    }
  )

  val defaultForm: Form[KeywordSearchForm] = form

  def fromRequest(implicit request: play.api.mvc.Request[_]): Form[KeywordSearchForm] =
    form.bindFromRequest(normalize(request))

  def unbind(data: KeywordSearchForm): Map[String, String] = form.mapping.unbind(data)

  private val SEARCH_QUERY_PATTERN = """"([^"]+)"""".r

  def parseSearchQuery(q: String): Seq[String] = {
    val keywords = new ArrayBuffer[String]
    SEARCH_QUERY_PATTERN.findAllIn(q).matchData.foreach { md =>
      keywords.append(md.group(1).split(" ")
        .map(StringUtils.stripToEmpty).filter(!_.isEmpty).mkString(" "))
    }
    keywords.append(SEARCH_QUERY_PATTERN.replaceAllIn(q, "").split(" "): _*)
    keywords.map(StringUtils.stripToEmpty).filter(!_.isEmpty).toList
  }

  def convertToSearchQuery(keywords: Seq[String]): String = {
    keywords.map { x =>
      if (x.contains(" ")) s""""${x}""""
      else x
    }.mkString(" ")
  }
}
