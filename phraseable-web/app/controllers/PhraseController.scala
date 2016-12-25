package controllers

import javax.inject.Inject

import controllers.action.{MemberAction, UserAction}
import models.form.PhraseEditForm
import models.{IdSequence, IdSequenceDao, Phrase, PhraseDao}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller

class PhraseController @Inject() (
  userAction: UserAction,
  memberAction: MemberAction,
  idSequenceDao: IdSequenceDao,
  phraseDao: PhraseDao,
  val messagesApi: MessagesApi
) extends Controller with I18nSupport {

  private val FLASH_POST_EDIT = "PhraseController.postEdit"

  def index = TODO

  def edit(id: Option[Long]) = (userAction andThen memberAction) { implicit request =>

    val flash = request.flash.data.get(FLASH_POST_EDIT)

    id.map { phraseId =>
      phraseDao.find(phraseId).map { phrase =>
        val data = PhraseEditForm(
          Some(phrase.id), phrase.lang.name, phrase.content, phrase.description)
        Ok(views.html.phrase.edit(PhraseEditForm.defaultForm.fill(data), flash))
      }.getOrElse {
        NotFound
      }
    }.getOrElse {
      Ok(views.html.phrase.edit(PhraseEditForm.defaultForm, flash))
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
                description = data.description
              )
            )
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
              data.content, data.description)
          )
          Redirect(routes.PhraseController.edit(Some(phrase.id)))
            .flashing(FLASH_POST_EDIT -> "created")
        }
      }
    )
  }
}
