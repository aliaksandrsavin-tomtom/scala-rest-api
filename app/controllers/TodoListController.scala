package controllers

import models.{NewTodoListItem, TodoListItem}
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.collection.mutable

@Singleton
class TodoListController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  implicit val todoListJson: OFormat[TodoListItem] = Json.format[TodoListItem]
  implicit val newTodoListJson: OFormat[NewTodoListItem] = Json.format[NewTodoListItem]

  private val todoList = new mutable.ListBuffer[TodoListItem]()
  todoList += TodoListItem(1, "test", isItDone = true)
  todoList += TodoListItem(2, "some other value", isItDone = false)

  def getAll: Action[AnyContent] = Action {
    if (todoList.isEmpty) {
      NoContent
    } else {
      Ok(Json.toJson(todoList))
    }
  }

  def getById(itemId: Long): Action[AnyContent] = Action {
    val foundItem = todoList.find(_.id == itemId)
    foundItem match {
      case Some(item) => Ok(Json.toJson(item))
      case None => NotFound
    }
  }

  def addNewItem(): Action[AnyContent] = Action { implicit request =>
    val content = request.body
    val jsonObject = content.asJson
    val todoListItem: Option[NewTodoListItem] =
      jsonObject.flatMap(Json.fromJson[NewTodoListItem](_).asOpt)

    todoListItem match {
      case Some(newItem) =>
        val nextId = todoList.map(_.id).max + 1
        val toBeAdded = TodoListItem(nextId, newItem.description, isItDone = false)
        todoList += toBeAdded
        Created(Json.toJson(toBeAdded))
      case None =>
        BadRequest
    }
  }

  def markAsDone(itemId: Long): Action[AnyContent] = Action {
    val foundItem = todoList.find(_.id == itemId)
    foundItem match {
      case Some(item) =>
        item.isItDone = true
        Ok(Json.toJson(item))
      case None =>
        NotFound
    }
  }

  def deleteAllDone(): Action[AnyContent] = Action {
    val initialSize = todoList.size
    val itemsToBeRemoved = todoList.filter(_.isItDone)
    todoList --= itemsToBeRemoved
    val newSize = todoList.size

    Ok((initialSize - newSize) + " elements removed")
  }
}
