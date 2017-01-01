package models.form

import org.apache.commons.lang3.StringUtils
import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._

case class PhraseSearchForm(
  keywords: Seq[String] = Seq.empty,
  offset: Option[Long] = None,
  limit: Option[Long] = None,
  orderBy: Option[String] = None
)

object PhraseSearchForm extends NormalizationSupport {

  private val KEYWORD_SEPARATOR = " "

  private val form = Form(
    mapping(
      "q" -> optional(text),
      "offset" -> optional(of[Long]),
      "limit" -> optional(of[Long]),
      "order" -> optional(text)
    ) { (q, offset, limit, order) =>
      val keywords = q.map { x =>
        x.split(KEYWORD_SEPARATOR).map(StringUtils.stripToEmpty).filter(!_.isEmpty).toSeq
      }.getOrElse(Seq.empty)
      PhraseSearchForm(keywords, offset, limit, order)
    } { a =>
      val q = if (a.keywords.isEmpty) None else Some(a.keywords.mkString(KEYWORD_SEPARATOR))
      Some(q, a.offset, a.limit, a.orderBy)
    }
  )

  val defaultForm: Form[PhraseSearchForm] = form

  def fromRequest(implicit request: play.api.mvc.Request[_]): Form[PhraseSearchForm] =
    form.bindFromRequest(normalize(request))

  def unbind(data: PhraseSearchForm): Map[String, String] = form.mapping.unbind(data)
}
