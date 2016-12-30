package models.form

import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._

case class CategorySearchForm(
  keyword: Option[String],
  offset: Option[Long],
  limit: Option[Long],
  orderBy: Option[String]
)

object CategorySearchForm extends NormalizationSupport {

  private val form = Form(
    mapping(
      "q" -> optional(text),
      "offset" -> optional(of[Long]),
      "limit" -> optional(of[Long]),
      "order" -> optional(text)
    )(CategorySearchForm.apply)(CategorySearchForm.unapply)
  )

  val defaultForm: Form[CategorySearchForm] = form

  def fromRequest(implicit request: play.api.mvc.Request[_]): Form[CategorySearchForm] =
    form.bindFromRequest(normalize(request))
}
