package controllers

import javax.inject.Inject

import controllers.action.{MemberAction, UserAction}
import controllers.session.UserSessionFactory
import models._
import models.form.{PhraseEditForm, PhraseSearchForm}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller

class PhraseController @Inject() (
  userAction: UserAction,
  memberAction: MemberAction,
  idSequenceDao: IdSequenceDao,
  phraseDao: PhraseDao,
  categoryDao: CategoryDao,
  relPhraseCategoryDao: RelPhraseCategoryDao,
  phraseSelectDao: PhraseSelectDao,
  userSessionFactory: UserSessionFactory,
  val messagesApi: MessagesApi
) extends Controller with I18nSupport {

  private val FLASH_POST_EDIT = "PhraseController.postEdit"

  private val DEFAULT_PHRASE_SELECT_LIMIT = 20

  lazy private val phraseSearchSession = userSessionFactory.create("PhraseSearch")

  def index() = userAction { implicit userRequest =>
    // Bind session and request parameters
    val m = Seq("offset", "limit", "order").foldLeft(
      phraseSearchSession.read(userRequest.sessionId)
    ) { (acc, k) =>
      val v = userRequest.getQueryString(k)
      if (v.isDefined) acc.updated(k, v.get)
      else acc
    }
    val data = PhraseSearchForm.defaultForm.bind(m).fold(
      _ => PhraseSearchForm(),
      identity
    )
    val pagination = phraseSelectDao.selectByCondition(
      PhraseSelectDao.Condition(None, data.categoryTitles.headOption),
      data.offset.getOrElse(0), data.limit.getOrElse(DEFAULT_PHRASE_SELECT_LIMIT), None
    )
    phraseSearchSession.update(
      userRequest.sessionId,
      PhraseSearchForm.defaultForm.mapping.unbind(data.copy(offset = Some(pagination.offset)))
    )
    Ok(views.html.phrase.index(pagination))
  }

  def search() = userAction { implicit userRequest =>
    val data = PhraseSearchForm.fromRequest.fold(
      _ => PhraseSearchForm(),
      identity
    )
    val m = PhraseSearchForm.defaultForm.mapping.unbind(data)
    phraseSearchSession.update(userRequest.sessionId, m)
    Redirect(routes.PhraseController.index())
  }

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
