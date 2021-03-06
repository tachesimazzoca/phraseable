package controllers

import javax.inject.Inject

import components.util.Pagination
import controllers.action.{MemberAction, UserAction}
import controllers.session.UserSessionFactory
import models._
import models.form.{KeywordSearchForm, PhraseEditForm, PhraseUploadForm}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller

class PhraseController @Inject() (
  userAction: UserAction,
  memberAction: MemberAction,
  phraseService: PhraseService,
  phraseUploader: PhraseUploadService,
  phraseSelectDao: PhraseSelectDao,
  userSessionFactory: UserSessionFactory,
  val messagesApi: MessagesApi
) extends Controller with I18nSupport {

  private val FLASH_POST_EDIT = "PhraseController.postEdit"
  private val FLASH_POST_UPLOAD = "PhraseController.postUpload"

  private val DEFAULT_PHRASE_SELECT_LIMIT = 50
  private val DEFAULT_PHRASE_SELECT_ORDER_BY = PhraseSelectDao.OrderBy.TermAsc
  private val PHRASE_SELECT_ORDER_BY_MAP = Map(
    "content_asc" -> PhraseSelectDao.OrderBy.TermAsc
  )
  lazy private val phraseSearchSession = userSessionFactory.create("PhraseSearch")

  def index() = userAction { implicit userRequest =>
    // Merge saved search session with query parameters
    val saved = KeywordSearchForm.defaultForm
      .bind(phraseSearchSession.read(userRequest.sessionId))
      .fold(_ => KeywordSearchForm(), identity)
    val merged = saved.copy(
      offset = userRequest.getQueryString("offset")
        .map(Pagination.parseOffset(_, 0)).orElse(saved.offset),
      limit = userRequest.getQueryString("limit")
        .map(Pagination.parseLimit(_, DEFAULT_PHRASE_SELECT_LIMIT)).orElse(saved.limit),
      orderBy = userRequest.getQueryString("order").orElse(saved.orderBy)
    )

    // Retrieve rows with pagination result
    val pagination = phraseSelectDao.selectByCondition(
      PhraseSelectDao.Condition(Seq.empty, merged.keywords),
      merged.offset.getOrElse(0),
      merged.limit.getOrElse(DEFAULT_PHRASE_SELECT_LIMIT),
      None
    )
    val keywordSearchForm = KeywordSearchForm.defaultForm.fill(
      merged.copy(offset = Some(pagination.offset)))

    // Store the search condition
    phraseSearchSession.update(userRequest.sessionId, keywordSearchForm.data)

    Ok(views.html.phrase.index(pagination, keywordSearchForm))
  }

  def search() = userAction { implicit userRequest =>
    val data = KeywordSearchForm.fromRequest.fold(
      _ => KeywordSearchForm(), identity)
    phraseSearchSession.update(userRequest.sessionId, KeywordSearchForm.unbind(data))
    Redirect(routes.PhraseController.index())
  }

  def detail(id: Long) = userAction {
    phraseService.find(id).map { case (phrase, categories) =>
      Ok(views.html.phrase.detail(phrase, categories))
    }.getOrElse {
      NotFound
    }
  }

  def delete(id: Long) = (userAction andThen memberAction) {
    phraseService.delete(id)
    Redirect(routes.PhraseController.index())
  }

  def edit(id: Option[Long]) = (userAction andThen memberAction) { implicit request =>

    val flash = request.flash.data.get(FLASH_POST_EDIT)

    id.map { phraseId =>
      phraseService.find(phraseId).map { case (phrase, categories) =>
        val data = PhraseEditForm(
          Some(phrase.id), phrase.lang.name, phrase.term,
          phrase.translation, phrase.description,
          categories.map(_.title)
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
          phraseService.find(phraseId).map { case (phrase, _) =>
            phraseService.update(
              phrase.copy(
                lang = Phrase.Lang.fromName(data.lang),
                term = data.term,
                translation = data.translation,
                description = data.description
              ),
              data.categoryTitles
            )
            Redirect(routes.PhraseController.edit(Some(phrase.id)))
              .flashing(FLASH_POST_EDIT -> "updated")

          }.getOrElse {
            NotFound
          }

        }.getOrElse {
          // Create a new entry
          val phraseId = phraseService.nextPhraseId()
          phraseService.create(
            Phrase(phraseId, Phrase.Lang.fromName(data.lang),
              data.term, data.translation, data.description),
            data.categoryTitles
          )
          Redirect(routes.PhraseController.edit(Some(phraseId)))
            .flashing(FLASH_POST_EDIT -> "created")
        }
      }
    )
  }

  def upload = (userAction andThen memberAction) { implicit request =>
    val flash = request.flash.data.get(FLASH_POST_UPLOAD)
    Ok(views.html.phrase.upload(PhraseUploadForm.defaultForm, Seq.empty, flash))
  }

  def postUpload = (userAction andThen memberAction) (
    parse.multipartFormData) { implicit request =>

    request.body.file("data").map { tempFile =>
      val m = PhraseUploadForm.fromRequest.data
      PhraseUploadForm.defaultForm.bind(
        m.updated(
          "contentType", tempFile.contentType.filter(_.startsWith("text/")).isDefined.toString
        )
      ).fold(
        form => BadRequest(views.html.phrase.upload(form)),
        data => {
          val file = tempFile.ref.file
          val uploaded = phraseUploader.upload(new java.io.FileInputStream(file), data.truncate)
          file.delete()
          uploaded match {
            case Left(x) =>
              BadRequest(views.html.phrase.upload(PhraseUploadForm.defaultForm, x))
            case Right(_) =>
              Redirect(routes.PhraseController.upload()).flashing(FLASH_POST_UPLOAD -> "uploaded")
          }
        }
      )
    }.getOrElse {
      Redirect(routes.PhraseController.upload())
    }
  }
}
