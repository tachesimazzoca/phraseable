package controllers

import javax.inject.Inject

import components.util.Pagination
import controllers.action.{MemberAction, UserAction}
import controllers.session.UserSessionFactory
import models._
import models.form.{CategoryEditForm, CategorySearchForm, PhraseSearchForm}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller

class CategoryController @Inject() (
  userAction: UserAction,
  memberAction: MemberAction,
  idSequenceDao: IdSequenceDao,
  phraseSelectDao: PhraseSelectDao,
  categoryDao: CategoryDao,
  relPhraseCategoryDao: RelPhraseCategoryDao,
  categorySelectDao: CategorySelectDao,
  userSessionFactory: UserSessionFactory,
  val messagesApi: MessagesApi
) extends Controller with I18nSupport {

  private val FLASH_POST_EDIT = "CategoryController.postEdit"

  private val DEFAULT_CATEGORY_SELECT_LIMIT = 50
  private val DEFAULT_CATEGORY_SELECT_ORDER_BY = CategorySelectDao.OrderBy.TitleAsc
  private val CATEGORY_SELECT_ORDER_BY_MAP = Map(
    "title_asc" -> CategorySelectDao.OrderBy.TitleAsc,
    "phase_count_desc" -> CategorySelectDao.OrderBy.PhraseCountDesc
  )
  lazy private val categorySearchSession = userSessionFactory.create("CategoryController.search")

  private val DEFAULT_PHRASE_SELECT_LIMIT = 50
  private val DEFAULT_PHRASE_SELECT_ORDER_BY = PhraseSelectDao.OrderBy.TermAsc
  private val PHRASE_SELECT_ORDER_BY_MAP = Map(
    "term_asc" -> PhraseSelectDao.OrderBy.TermAsc
  )
  lazy private val categoryDetailSession = userSessionFactory.create("CategoryController.detail")

  def index() = userAction { implicit userRequest =>
    // Merge saved search session with query parameters
    val saved = CategorySearchForm.defaultForm
      .bind(categorySearchSession.read(userRequest.sessionId))
      .fold(_ => CategorySearchForm(), identity)
    val merged = saved.copy(
      offset = userRequest.getQueryString("offset")
        .map(Pagination.parseOffset(_, 0)).orElse(saved.offset),
      limit = userRequest.getQueryString("limit")
        .map(Pagination.parseLimit(_, DEFAULT_CATEGORY_SELECT_LIMIT))
        .orElse(saved.limit),
      orderBy = userRequest.getQueryString("order").orElse(saved.orderBy)
    )

    // Retrieve rows with pagination result
    val pagination = categorySelectDao.selectByCondition(
      CategorySelectDao.Condition(merged.keywords),
      merged.offset.getOrElse(0),
      merged.limit.getOrElse(DEFAULT_CATEGORY_SELECT_LIMIT),
      merged.orderBy.map(CATEGORY_SELECT_ORDER_BY_MAP)
        .orElse(Some(DEFAULT_CATEGORY_SELECT_ORDER_BY))
    )
    val categorySearchForm = CategorySearchForm.defaultForm.fill(
      merged.copy(offset = Some(pagination.offset)))

    // Store the search condition
    categorySearchSession.update(userRequest.sessionId, categorySearchForm.data)

    Ok(views.html.category.index(pagination, categorySearchForm))
  }

  def search() = userAction { implicit userRequest =>
    val data = CategorySearchForm.fromRequest.fold(
      _ => CategorySearchForm(), identity)
    categorySearchSession.update(userRequest.sessionId, CategorySearchForm.unbind(data))
    Redirect(routes.CategoryController.index())
  }

  def detail(id: Long) = userAction { implicit userRequest =>

    categoryDao.find(id).map { category =>

      // Merge saved search session with query parameters
      val saved = PhraseSearchForm.defaultForm
        .bind(categoryDetailSession.read(userRequest.sessionId))
        .fold(_ => PhraseSearchForm(), identity)
      val merged = saved.copy(
        offset = userRequest.getQueryString("offset")
          .map(Pagination.parseOffset(_, 0)).orElse(saved.offset),
        limit = userRequest.getQueryString("limit")
          .map(Pagination.parseLimit(_, DEFAULT_PHRASE_SELECT_LIMIT))
          .orElse(saved.limit),
        orderBy = userRequest.getQueryString("order").orElse(saved.orderBy)
      )

      val pagination = phraseSelectDao.selectByCondition(
        PhraseSelectDao.Condition(categoryIds = Seq(category.id)),
        merged.offset.getOrElse(0),
        merged.limit.getOrElse(DEFAULT_PHRASE_SELECT_LIMIT),
        merged.orderBy.map(PHRASE_SELECT_ORDER_BY_MAP)
          .orElse(Some(DEFAULT_PHRASE_SELECT_ORDER_BY))
      )
      val phraseSearchForm = PhraseSearchForm.defaultForm.fill(
        merged.copy(offset = Some(pagination.offset)))
      // Store the search condition
      categoryDetailSession.update(userRequest.sessionId, phraseSearchForm.data)

      Ok(views.html.category.detail(category, pagination))

    }.getOrElse {
      NotFound
    }
  }

  def delete(id: Long) = (userAction andThen memberAction) {
    categoryDao.delete(id)
    Redirect(routes.CategoryController.index())
  }

  def edit(id: Option[Long]) = (userAction andThen memberAction) { implicit request =>

    val flash = request.flash.data.get(FLASH_POST_EDIT)

    id.map { categoryId =>
      categoryDao.find(categoryId).map { category =>
        val data = CategoryEditForm(
          Some(category.id), category.title, category.description)
        Ok(views.html.category.edit(CategoryEditForm.defaultForm.fill(data), flash))
      }.getOrElse {
        NotFound
      }
    }.getOrElse {
      Ok(views.html.category.edit(CategoryEditForm.defaultForm))
    }
  }

  def postEdit = (userAction andThen memberAction) { implicit request =>
    CategoryEditForm.fromRequest.fold(
      form => BadRequest(views.html.category.edit(form)),
      data => {
        data.id.map { categoryId =>
          // Update the stored entry
          categoryDao.find(categoryId).map { category =>
            categoryDao.update(
              category.copy(
                title = data.title,
                description = data.description
              )
            )
            Redirect(routes.CategoryController.edit(Some(category.id)))
              .flashing(FLASH_POST_EDIT -> "updated")

          }.getOrElse {
            NotFound
          }

        }.getOrElse {
          // Create a new entry
          if (categoryDao.findByTitle(data.title).isDefined) {
            val formWithError = CategoryEditForm.defaultForm.bind(
              CategoryEditForm.unbind(data).updated("uniqueTitle", "false"))
            BadRequest(views.html.category.edit(formWithError))
          } else {
            val categoryId = idSequenceDao.nextId(IdSequence.SequenceType.Category)
            val category = categoryDao.create(
              Category(categoryId, data.title, data.description))
            Redirect(routes.CategoryController.edit(Some(category.id)))
              .flashing(FLASH_POST_EDIT -> "created")
          }
        }
      }
    )
  }
}
