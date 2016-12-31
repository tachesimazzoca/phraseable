package models.form

import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._

case class PhraseSearchForm(
  categoryTitles: Seq[String] = Seq.empty,
  offset: Option[Long] = None,
  limit: Option[Long] = None,
  orderBy: Option[String] = None
)

object PhraseSearchForm extends NormalizationSupport {

  private val form = Form(
    mapping(
      "categoryTitles" -> seq(text),
      "offset" -> optional(of[Long]),
      "limit" -> optional(of[Long]),
      "order" -> optional(text)
    )(PhraseSearchForm.apply)(PhraseSearchForm.unapply)
  )

  val defaultForm: Form[PhraseSearchForm] = form

  def fromRequest(implicit request: play.api.mvc.Request[_]): Form[PhraseSearchForm] =
    form.bindFromRequest(normalize(request))
}
