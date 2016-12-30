package controllers

import javax.inject.Inject

import controllers.action.{MemberAction, UserAction}
import models._
import models.form.CategoryEditForm
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller

class CategoryController @Inject() (
  userAction: UserAction,
  memberAction: MemberAction,
  idSequenceDao: IdSequenceDao,
  phraseDao: PhraseDao,
  categoryDao: CategoryDao,
  relPhraseCategoryDao: RelPhraseCategoryDao,
  val messagesApi: MessagesApi
) extends Controller with I18nSupport {

  private val FLASH_POST_EDIT = "CategoryController.postEdit"

  def index = TODO

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
