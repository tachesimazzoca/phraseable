package models

case class PhraseTranslation(
  id: Long,
  phraseId: Long,
  locale: String,
  content: String,
  createdAt: Option[java.util.Date] = None,
  updatedAt: Option[java.util.Date] = None
)
