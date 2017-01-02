package models

import javax.inject.Inject

class PhraseService @Inject() (
  idSequenceDao: IdSequenceDao,
  phraseDao: PhraseDao,
  categoryDao: CategoryDao,
  relPhraseCategoryDao: RelPhraseCategoryDao
) {

  def find(id: Long): Option[(Phrase, Seq[Category])] =
    phraseDao.find(id).map { phrase =>
      val categories = categoryDao.selectByPhraseId(phrase.id)
      (phrase, categories)
    }

  def delete(id: Long): Unit = {
    phraseDao.delete(id)
    // TODO: delete rows of rel_phrase_category
  }

  def truncate(): Unit = {
    relPhraseCategoryDao.truncate()
    categoryDao.truncate()
    phraseDao.truncate()
  }

  def create(
    phrase: Phrase, categoryTitles: Seq[String]
  ): Option[(Phrase, Seq[Category])] = {
    phraseDao.create(phrase)
    updateRelPhraseCategoryRows(phrase.id, categoryTitles)
    find(phrase.id)
  }

  def update(
    phrase: Phrase, categoryTitles: Seq[String]
  ): Option[(Phrase, Seq[Category])] = {
    phraseDao.update(phrase)
    updateRelPhraseCategoryRows(phrase.id, categoryTitles)
    find(phrase.id)
  }

  def nextPhraseId(): Long = idSequenceDao.nextId(IdSequence.SequenceType.Phrase)

  private def updateRelPhraseCategoryRows(
    phraseId: Long, categoryTitles: Seq[String]): Unit = {
    val categoryIds = categoryTitles.foldLeft(Seq.empty[Long]) { (acc, title) =>
      categoryDao.findByTitle(title).map { category =>
        acc :+ category.id
      }.getOrElse {
        val categoryId = idSequenceDao.nextId(IdSequence.SequenceType.Category)
        categoryDao.create(Category(categoryId, title, ""))
        acc :+ categoryId
      }
    }
    relPhraseCategoryDao.updateByPhraseId(phraseId, categoryIds)
  }
}
