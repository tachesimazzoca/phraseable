package models.form

import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._

case class CategoryEditForm(
  id: Option[Long],
  title: String,
  description: String
)

object CategoryEditForm extends NormalizationSupport {

  import ConstraintHelper._

  override val nonBlankFields: Seq[String] = Seq("title")

  private val form = Form(
    mapping(
      "id" -> optional(of[Long]),
      "title" -> text.verifying(nonBlank("CategoryEditForm.error.title")),
      "description" -> text
    )(CategoryEditForm.apply)(CategoryEditForm.unapply)
  )

  def defaultForm: Form[CategoryEditForm] = form

  def fromRequest(implicit request: play.api.mvc.Request[_]): Form[CategoryEditForm] =
    form.bindFromRequest(normalize(request))
}
