package controllers

import javax.inject.Inject

import controllers.action.{MemberAction, UserAction}
import models.form.PhraseEditForm
import models._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller

class PhraseController @Inject() (
  userAction: UserAction,
  memberAction: MemberAction,
  idSequenceDao: IdSequenceDao,
  phraseDao: PhraseDao,
  categoryDao: CategoryDao,
  relPhraseCategoryDao: RelPhraseCategoryDao,
  val messagesApi: MessagesApi
) extends Controller with I18nSupport {

  private val FLASH_POST_EDIT = "PhraseController.postEdit"

  def index = TODO

  def detail(id: Long) = userAction {
    phraseDao.find(id).map { phrase =>
      val categories = categoryDao.selectByPhraseId(phrase.id)
      Ok(views.html.phrase.detail(phrase, categories))
    }.getOrElse {
      NotFound
    }
  }

  def edit(id: Option[Long]) = (userAction andThen memberAction) { implicit request =>

    val flash = request.flash.data.get(FLASH_POST_EDIT)

    id.map { phraseId =>
      phraseDao.find(phraseId).map { phrase =>
        val data = PhraseEditForm(
          Some(phrase.id), phrase.lang.name, phrase.content,
          phrase.definition, phrase.description,
          categoryDao.selectByPhraseId(phraseId).map(_.title)
        )
        Ok(views.html.phrase.edit(PhraseEditForm.defaultForm.fill(data), flash))
      }.getOrElse {
        NotFound
      }
    }.getOrElse {
      Ok(views.html.phrase.edit(PhraseEditForm.defaultForm))
    }
  }

  def postEdit = (userAction andThen memberAction) { implicit request =>
    PhraseEditForm.fromRequest.fold(
      form => BadRequest(views.html.phrase.edit(form)),
      data => {
        data.id.map { phraseId =>
          // Update the stored entry
          phraseDao.find(phraseId).map { phrase =>
            phraseDao.update(
              phrase.copy(
                lang = Phrase.Lang.fromName(data.lang),
                content = data.content,
                definition = data.definition,
                description = data.description
              )
            )
            updateRelPhraseCategoryRows(phraseId, data.categoryTitles)
            Redirect(routes.PhraseController.edit(Some(phrase.id)))
              .flashing(FLASH_POST_EDIT -> "updated")

          }.getOrElse {
            NotFound
          }

        }.getOrElse {
          // Create a new entry
          val phraseId = idSequenceDao.nextId(IdSequence.SequenceType.Phrase)
          val phrase = phraseDao.create(
            Phrase(phraseId, Phrase.Lang.fromName(data.lang),
              data.content, data.definition, data.description)
          )
          updateRelPhraseCategoryRows(phraseId, data.categoryTitles)
          Redirect(routes.PhraseController.edit(Some(phrase.id)))
            .flashing(FLASH_POST_EDIT -> "created")
        }
      }
    )
  }

  private def updateRelPhraseCategoryRows(phraseId: Long, categoryTitles: Seq[String]) = {
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
