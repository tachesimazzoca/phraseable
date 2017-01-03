package models.form

import org.apache.commons.lang3.StringUtils
import play.api.data.Forms._
import play.api.data._

case class KeywordSearchForm(
  keywords: Seq[String] = Seq.empty,
  offset: Option[Long] = None,
  limit: Option[Long] = None,
  orderBy: Option[String] = None
)

object KeywordSearchForm extends NormalizationSupport {

  private val KEYWORD_SEPARATOR = " "

  private val form = Form(
    mapping(
      "q" -> optional(text),
      "offset" -> optional(longNumber),
      "limit" -> optional(longNumber),
      "order" -> optional(text)
    ) { (q, offset, limit, order) =>
      val keywords = q.map { x =>
        x.split(KEYWORD_SEPARATOR).map(StringUtils.stripToEmpty).filter(!_.isEmpty).toSeq
      }.getOrElse(Seq.empty)
      KeywordSearchForm(keywords, offset, limit, order)
    } { a =>
      val q = if (a.keywords.isEmpty) None else Some(a.keywords.mkString(KEYWORD_SEPARATOR))
      Some(q, a.offset, a.limit, a.orderBy)
    }
  )

  val defaultForm: Form[KeywordSearchForm] = form

  def fromRequest(implicit request: play.api.mvc.Request[_]): Form[KeywordSearchForm] =
    form.bindFromRequest(normalize(request))

  def unbind(data: KeywordSearchForm): Map[String, String] = form.mapping.unbind(data)
}
