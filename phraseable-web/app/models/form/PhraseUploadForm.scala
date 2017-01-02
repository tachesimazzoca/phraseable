package models.form

import play.api.data.Forms._
import play.api.data._

case class PhraseUploadForm(
  truncate: Boolean = false,
  contentType: Boolean = true,
  size: Boolean = true,
  accepted: Boolean = true
)

object PhraseUploadForm extends NormalizationSupport {

  import ConstraintHelper._

  private val form = Form(
    mapping(
      "truncate" -> default(boolean, false),
      "contentType" -> default(boolean, true)
        .verifying(passed("PhraseUploadForm.error.contentType")),
      "size" -> default(boolean, true)
        .verifying(passed("PhraseUploadForm.error.size")),
      "accepted" -> default(boolean, true)
        .verifying(passed("PhraseUploadForm.error.accepted"))
    )(PhraseUploadForm.apply)(PhraseUploadForm.unapply)
  )

  def defaultForm: Form[PhraseUploadForm] = form

  def fromRequest(implicit request: play.api.mvc.Request[_]): Form[PhraseUploadForm] =
    form.bindFromRequest(normalize(request))
}
