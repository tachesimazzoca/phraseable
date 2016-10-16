package models

case class Category(
  id: Long,
  title: String,
  description: String,
  createdAt: Option[java.util.Date] = None,
  updatedAt: Option[java.util.Date] = None
)
