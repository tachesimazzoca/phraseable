package models.form

import models.Phrase
import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._

case class PhraseEditForm(
  id: Option[Long],
  lang: String,
  content: String,
  definition: String,
  description: String
)

object PhraseEditForm extends NormalizationSupport {

  import ConstraintHelper._

  override val nonBlankFields: Seq[String] = Seq("content")

  private val form = Form(
    mapping(
      "id" -> optional(of[Long]),
      "lang" -> text.verifying(nonBlank("PhraseEditForm.error.lang")),
      "content" -> text.verifying(nonBlank("PhraseEditForm.error.content")),
      "definition" -> text,
      "description" -> text
    )(PhraseEditForm.apply)(PhraseEditForm.unapply)
  )

  val langMap = Phrase.Lang.supportedLanguages.map(x => (x.name, x.value)).toMap

  def defaultForm: Form[PhraseEditForm] = form

  def fromRequest(implicit request: play.api.mvc.Request[_]): Form[PhraseEditForm] =
    form.bindFromRequest(normalize(request))
}
