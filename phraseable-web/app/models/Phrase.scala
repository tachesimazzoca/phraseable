package models

case class Phrase(
  id: Long,
  content: String,
  description: String,
  createdAt: Option[java.util.Date] = None,
  updatedAt: Option[java.util.Date] = None
)
