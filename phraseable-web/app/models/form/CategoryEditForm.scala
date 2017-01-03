package models.form

import play.api.data.Forms._
import play.api.data._

case class CategoryEditForm(
  id: Option[Long],
  title: String,
  description: String,
  uniqueTitle: Boolean = true
)

object CategoryEditForm extends NormalizationSupport {

  import ConstraintHelper._

  override val nonBlankFields: Seq[String] = Seq("title")

  private val form = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> text.verifying(nonBlank("CategoryEditForm.error.title")),
      "description" -> text,
      "uniqueTitle" -> default(boolean, true)
        .verifying(passed("CategoryEditForm.error.uniqueTitle"))
    )(CategoryEditForm.apply)(CategoryEditForm.unapply)
  )

  def defaultForm: Form[CategoryEditForm] = form

  def fromRequest(implicit request: play.api.mvc.Request[_]): Form[CategoryEditForm] =
    form.bindFromRequest(normalize(request))

  def unbind(data: CategoryEditForm): Map[String, String] = form.mapping.unbind(data)
}
