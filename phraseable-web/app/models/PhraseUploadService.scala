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
    .withHeader("content", "definition", "description", "categories")
    .withSkipHeaderRecord()

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
          "lang" -> "en",
          "content" -> m.getOrElse("content", ""),
          "definition" -> m.getOrElse("definition", ""),
          "description" -> m.getOrElse("description", ""),
          "categoryTitlesText" -> m.getOrElse("categories", "")
        )
      ).fold(
        form => {
          if (errors.size < 10)
            errors.append(m)
        },
        data => {
          val phraseId = phraseService.nextPhraseId()
          phraseService.create(
            Phrase(
              phraseId, Phrase.Lang.fromName(data.lang),
              data.content, data.definition, data.description
            ),
            data.categoryTitles
          )
          n = n + 1
        }
      )
    }
    if (errors.isEmpty) Right(n)
    else Left(errors)
  }
}
