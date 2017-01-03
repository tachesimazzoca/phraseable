package models

import java.io.{InputStream, InputStreamReader}
import javax.inject.Inject

import models.form.PhraseEditForm
import org.apache.commons.csv.CSVFormat

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

class PhraseUploadService @Inject() (
  phraseService: PhraseService
) {

  val csvFormat = CSVFormat.DEFAULT.withDelimiter('\t')
    .withHeader("id", "lang", "content", "definition", "description", "categories")
    .withSkipHeaderRecord()

  private val MAX_ERROR_REPORTING = 10

  def upload(
    input: InputStream,
    truncate: Boolean = false
  ): Either[Seq[Map[String, String]], Int] = {

    if (truncate)
      phraseService.truncate()

    val errors = new ArrayBuffer[Map[String, String]]
    var n = 0
    for (row <- csvFormat.parse(new InputStreamReader(input))) {
      val m = row.toMap.toMap
      PhraseEditForm.defaultForm.bind(
        Map(
          "id" -> m.getOrElse("id", ""),
          "lang" -> m.getOrElse("lang", Phrase.Lang.English.name),
          "content" -> m.getOrElse("content", ""),
          "definition" -> m.getOrElse("definition", ""),
          "description" -> m.getOrElse("description", ""),
          "categoryTitlesText" -> m.getOrElse("categories", "")
        )
      ).fold(
        form => {
          if (errors.size < MAX_ERROR_REPORTING)
            errors.append(m)
        },
        data => {
          data.id.map { phraseId =>
            phraseService.find(phraseId).map { case (phrase, _) =>
              phraseService.update(
                phrase.copy(
                  lang = Phrase.Lang.fromName(data.lang),
                  content = data.content,
                  definition = data.definition,
                  description = data.description
                ),
                data.categoryTitles
              )
            }
          }.getOrElse {
            val phraseId = phraseService.nextPhraseId()
            phraseService.create(
              Phrase(
                phraseId, Phrase.Lang.fromName(data.lang),
                data.content, data.definition, data.description
              ),
              data.categoryTitles
            )
          }
          n = n + 1
        }
      )
    }
    if (errors.isEmpty) Right(n)
    else Left(errors)
  }
}
