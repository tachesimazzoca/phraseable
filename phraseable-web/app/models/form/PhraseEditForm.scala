package models.form

import models.Phrase
import org.apache.commons.lang3.StringUtils
import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats._

case class PhraseEditForm(
  id: Option[Long],
  lang: String,
  content: String,
  definition: String,
  description: String,
  categoryTitles: Seq[String] = Seq.empty
)

object PhraseEditForm extends NormalizationSupport {

  import ConstraintHelper._

  private val CATEGORY_TAG_SEPARATOR: String = "\n"

  override val nonBlankFields: Seq[String] = Seq("content")

  private val form = Form(
    mapping(
      "id" -> optional(of[Long]),
      "lang" -> text.verifying(nonBlank("PhraseEditForm.error.lang")),
      "content" -> text.verifying(nonBlank("PhraseEditForm.error.content")),
      "definition" -> text,
      "description" -> text,
      "categoryTitlesText" -> text
    ) {
      // apply
      (
        id: Option[Long], lang: String, content: String,
        definition: String, description: String, categoryTitlesText: String
      ) =>
        val categoryTitles = categoryTitlesText.split(CATEGORY_TAG_SEPARATOR)
          .map(StringUtils.stripToEmpty).filter(!_.isEmpty)
        PhraseEditForm(id, lang, content, definition, description, categoryTitles)
    } {
      // unapply
      a: PhraseEditForm =>
        Some(a.id, a.lang, a.content, a.definition, a.description,
          a.categoryTitles.map(StringUtils.stripToEmpty).mkString(CATEGORY_TAG_SEPARATOR))
    }
  )

  val langOptions = Phrase.Lang.supportedLanguages.map(x => (x.name, x.value))

  def defaultForm: Form[PhraseEditForm] = form

  def fromRequest(implicit request: play.api.mvc.Request[_]): Form[PhraseEditForm] =
    form.bindFromRequest(normalize(request))
}
