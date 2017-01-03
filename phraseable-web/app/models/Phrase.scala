package models

case class Phrase(
  id: Long,
  lang: Phrase.Lang,
  term: String,
  translation: String,
  description: String,
  createdAt: Option[java.util.Date] = None,
  updatedAt: Option[java.util.Date] = None
)

object Phrase {

  sealed abstract class Lang(val name: String, val value: String)

  object Lang {

    case object English extends Lang("en", "English")

    case object Japanese extends Lang("ja", "Japanese")

    val supportedLanguages: Seq[Lang] = Seq(English, Japanese)

    def fromName(v: String): Lang = v match {
      case "en" => English
      case "ja" => Japanese
      case _ => English
    }
  }

}
