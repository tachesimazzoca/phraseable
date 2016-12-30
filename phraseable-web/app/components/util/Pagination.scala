package components.util

case class Pagination[T](rows: Seq[T], offset: Long, limit: Long, count: Long)

object Pagination {

  type CountFunction = () => Long

  type SelectFunction[T] = (Long, Long) => Seq[T]

  /**
    * Adjust the given offset to be smaller than the result of the count function
    * and then returns a pagination object with the select function.
    */
  def paginate[T](
    offset: Long, limit: Long,
    count: CountFunction, select: SelectFunction[T]
  ): Pagination[T] = {

    val c = count()

    if (c == 0) {
      Pagination(Seq.empty[T], 0, limit, 0)
    } else {
      val first = if (offset >= c) ((c - 1) * limit) / limit else 0
      Pagination(select(first, limit), first, limit, c)
    }
  }
}
