package models

import javax.inject.Inject

import scala.collection.mutable.ArrayBuffer

class PhraseService @Inject() (
  idSequenceDao: IdSequenceDao,
  phraseDao: PhraseDao,
  categoryDao: CategoryDao,
  relPhraseCategoryDao: RelPhraseCategoryDao,
  phraseKeywordDao: PhraseKeywordDao
) {

  import PhraseService._

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
    idSequenceDao.reset(IdSequence.SequenceType.Category)
    phraseDao.truncate()
    idSequenceDao.reset(IdSequence.SequenceType.Phrase)
  }

  def create(
    phrase: Phrase, categoryTitles: Seq[String]
  ): Option[(Phrase, Seq[Category])] = {
    phraseDao.create(phrase)
    updateRelPhraseCategoryRows(phrase.id, categoryTitles)
    val saved = find(phrase.id)
    saved.foreach { x =>
      updateKeywords(x._1, x._2)
    }
    saved
  }

  def update(
    phrase: Phrase, categoryTitles: Seq[String]
  ): Option[(Phrase, Seq[Category])] = {
    phraseDao.update(phrase)
    updateRelPhraseCategoryRows(phrase.id, categoryTitles)
    val saved = find(phrase.id)
    saved.foreach { x =>
      updateKeywords(x._1, x._2)
    }
    saved
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

  private def updateKeywords(phrase: Phrase, categories: Seq[Category]): Unit = {
    val keywords = new ArrayBuffer[String]

    // phrase.term
    if (!phrase.term.contains(" ")) {
      keywords.append(phrase.term)
    } else {
      keywords.append(parseKeywords(phrase.term): _*)
    }
    // phrase.translation
    if (!phrase.translation.contains(" ")) {
      keywords.append(phrase.translation)
    } else {
      keywords.append(parseKeywords(phrase.translation): _*)
    }
    // phrase.description
    keywords.append(parseKeywords(phrase.description): _*)

    phraseKeywordDao.updateKeywords(phrase.id, keywords.toSet.toSeq)
  }
}

object PhraseService {

  private val KEYWORD_BRACKET_PATTERN = """\{([^\{\}]+)\}""".r
  private val KEYWORD_SEPARATOR_PATTERN = "[\\|,]"

  def parseKeywords(data: String): Seq[String] = {
    // Parse keyword brackets notation { foo | bar }
    KEYWORD_BRACKET_PATTERN.findAllIn(data).matchData.flatMap { md =>
      md.group(1).split(KEYWORD_SEPARATOR_PATTERN).map(_.trim).filter(!_.isEmpty)
    }.toList
  }
}
