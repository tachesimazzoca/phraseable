package controllers

import javax.inject.Inject

import controllers.action.{MemberAction, UserAction}
import models._
import models.form.{CategoryEditForm, CategorySearchForm}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller

class CategoryController @Inject() (
  userAction: UserAction,
  memberAction: MemberAction,
  idSequenceDao: IdSequenceDao,
  phraseDao: PhraseDao,
  categoryDao: CategoryDao,
  relPhraseCategoryDao: RelPhraseCategoryDao,
  categorySelectDao: CategorySelectDao,
  val messagesApi: MessagesApi
) extends Controller with I18nSupport {

  private val FLASH_POST_EDIT = "CategoryController.postEdit"

  private val DEFAULT_CATEGORY_SELECT_ORDER_BY = CategorySelectDao.OrderBy.TitleAsc

  private val supportedOrderByMap = Map(
    "id_asc" -> CategorySelectDao.OrderBy.IdAsc,
    "title_asc" -> CategorySelectDao.OrderBy.TitleAsc,
    "phase_count_desc" -> CategorySelectDao.OrderBy.PhraseCountDesc
  )

  def index() = userAction { implicit userRequest =>
    CategorySearchForm.fromRequest.fold(
      form => BadRequest,
      data => {
        val pagination = categorySelectDao.selectCategories(
          data.keyword,
          data.offset.getOrElse(0),
          data.limit.getOrElse(10),
          data.orderBy.map(supportedOrderByMap))
        Ok(views.html.category.index(pagination))
      }
    )
  }

  def detail(id: Long) = userAction {
    categoryDao.find(id).map { category =>
      Ok(views.html.category.detail(category))
    }.getOrElse {
      NotFound
    }
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
          val categoryId = idSequenceDao.nextId(IdSequence.SequenceType.Category)
          val category = categoryDao.create(
            Category(categoryId, data.title, data.description))
          Redirect(routes.CategoryController.edit(Some(category.id)))
            .flashing(FLASH_POST_EDIT -> "created")
        }
      }
    )
  }
}
