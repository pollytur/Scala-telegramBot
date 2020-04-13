package projectbot

import com.bot4s.telegram.api.declarative.{Action, Callbacks, Commands}
import cats.instances.future._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.EditMessageReplyMarkup
import com.bot4s.telegram.models.{ChatId, InlineKeyboardButton, InlineKeyboardMarkup}
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.util.Try
import scala.concurrent.{Await, Future}
import cats.syntax.functor._
import cats.syntax.traverse._
import com.bot4s.telegram.api.BotBase
import com.bot4s.telegram.methods.AnswerCallbackQuery
import com.bot4s.telegram.models.CallbackQuery
import projectbot.MyTables.{CommonClasses, Labs, UserCourse, UserLab, Users}
import slick.driver.SQLiteDriver.api._
import slick.lifted.CanBeQueryCondition
import slick.model.Column

import scala.concurrent.duration._
//import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable

/** Generates random values.
 */
class ScheduleBot(val token: String,
                val coreLink : String,
                val electiveLink : String,
                val coreFile : String,
                val electiveFile : String,
                db :Database) extends TelegramBot
  with Polling
  with Commands[Future]
  with Callbacks[Future] {

  LoggerConfig.factory = PrintLoggerFactory()
  // set log level, e.g. to TRACE
  LoggerConfig.level = LogLevel.TRACE

  def createGroupButtons(): Seq[InlineKeyboardButton]={
    Parsing.groups(coreFile).map(e=>InlineKeyboardButton.callbackData(e,e))
  }

  def createElectiveButtons(): Seq[InlineKeyboardButton]={
    Parsing.listOfElectives(electiveFile).map(e=>InlineKeyboardButton.callbackData(e,e))
  }

  val groupButtons    : Seq[InlineKeyboardButton] = createGroupButtons()
  val electiveButtons : Seq[InlineKeyboardButton] = createElectiveButtons()
  val groupTags    : Seq[String] = Parsing.groups(coreFile)
  val electiveTags : Seq[String] = Parsing.listOfElectives(electiveFile)

  val labs = TableQuery[Labs]
  val cores = TableQuery[CommonClasses]
  val users = TableQuery[Users]
  val courses = TableQuery[UserCourse]
  val userLabs = TableQuery[UserLab]

  // Use sttp-based backend
  //  implicit val backend = SttpBackends.default
  //  override val client: RequestHandler[Future] = new FutureSttpClient(token)

  // Or just the scalaj-http backend
  override val client: RequestHandler[Future] = new ScalajHttpClient(token)

  val rng = new scala.util.Random(System.currentTimeMillis())

//todo first Bachelor or masters, than year than group filtered
  onCommand("set_group") {
    implicit msg =>
      reply( "Select your group number",

        replyMarkup = Some(InlineKeyboardMarkup
          .singleColumn(groupButtons)
        )
      ).void
  }

  onCommand("set_elective") {
    implicit msg =>
    reply( "Select your electives",
      replyMarkup = Some(InlineKeyboardMarkup
        .singleColumn(electiveButtons)
      )
    ).void
  }

  onCommand("core_link"){
    implicit msg =>
      reply( s"Core courses table is available here ${coreLink}").void
  }

  onCommand("electives_link"){
    implicit msg =>
      reply( s"Elective courses table is available here ${electiveLink}").void
  }

//  todo change
//  todo handle execptions is the user has no courses
  onCommand("my_courses"){
    implicit msg =>
      val theirCourses = courses.filter( _.id === msg.from.get.id).map(e=> e.class_id).result
      val f = db.run(theirCourses)
      Await.result(f, 2.second)
      val mew = f.value.get.get.mkString("\n")
      reply( s"Your courses \n ${mew}").void
  }



  def hasGroupTag(cbq: CallbackQuery, group:Seq[String]) : Option[String] ={
    (for (gr <- group.toStream if cbq.data.exists(_.startsWith(gr))) yield gr).headOption
  }


  onCallbackQuery { implicit cbq =>
    val res = hasGroupTag(cbq, groupTags)
    res match {
      case Some(value) =>{
        val ackFuture = ackCallback(Some( s"${cbq.from.firstName} pressed the button with ${value}"))(cbq)
        val insert = db.run(DBIO.seq(users += (cbq.from.id, value)))
        Await.result(insert, 1.second)
        Parsing.groupSubjects(coreFile, value) match{
          case Right(v) => {
            val unique = Parsing.uniqueCourses(v)
            for (u <- unique){
              val result = db.run(DBIO.seq(courses += (cbq.from.id, u)))
              Await.result(result, 1.second)
//              todo insert courses and labs in the appropriate tables
//              todo think about if not there?
            }
          }
        }
        val maybeEditFuture = for {
          msg <- cbq.message
          response <- Some(request(
            EditMessageReplyMarkup(
              Some(ChatId(msg.source)), // msg.chat.id
              Some(msg.messageId),
              replyMarkup = None)
          ))
        } yield response
        ackFuture.zip(maybeEditFuture.getOrElse(Future.successful(())))
          .void}

      case _=> {
        val elec = hasGroupTag(cbq, electiveTags)
        elec match {
          case Some(value)=>{
            val ackFuture = ackCallback(Some( s"${cbq.from.firstName} pressed the button with ${value}"))(cbq)
            val result = db.run(DBIO.seq(courses += (cbq.from.id, value)))
            Await.result(result, 1.second)
            val maybeEditFuture = for {
              msg <- cbq.message
              response <- Some(request(
                EditMessageReplyMarkup(
                  Some(ChatId(msg.source)), // msg.chat.id
                  Some(msg.messageId),
                  replyMarkup = None)
              ))
            } yield response
            ackFuture.zip(maybeEditFuture.getOrElse(Future.successful(())))
              .void
          }
        }
      }
      }
    }



//  def selectedGroup(tag:String, cbq:CallbackQuery) {
//      val ackFuture = ackCallback(Some(cbq.from.firstName + " pressed the button!"))(cbq)
//      val maybeEditFuture = for {
//        msg <- cbq.message
//        response <- Some(request(
//          EditMessageReplyMarkup(
//            Some(ChatId(msg.source)), // msg.chat.id
//            Some(msg.messageId),
//            replyMarkup = None)
//        ))
//      } yield response
//      ackFuture.zip(maybeEditFuture.getOrElse(Future.successful(())))
//        .void
//  }


//  onCallbackWithTag("B18-03-clicked") {
//    implicit cbq =>
//    // Notification only shown to the user who pressed the button.
//    val ackFuture = ackCallback(Some(cbq.from.firstName + " pressed the button!"))
//    // Or just ackCallback()
//
//    val maybeEditFuture = for {
////      data <- cbq.data
////      Int(n) = data
//      msg <- cbq.message
////      response <- Some(request(
////        EditMessageReplyMarkup(
////          Some(ChatId(msg.source)), // msg.chat.id
////          Some(msg.messageId),
//////          ReplyKeyboardRemove
////          replyMarkup = Some(InlineKeyboardMarkup.singleButton(
////            InlineKeyboardButton.callbackData("You clicked BS-1", "bss"))
////          ))
////      ))
//      response <- Some(request(
//                EditMessageReplyMarkup(
//                  Some(ChatId(msg.source)), // msg.chat.id
//                  Some(msg.messageId),
//        //          ReplyKeyboardRemove
//                  replyMarkup = None)
////              ))
//
////      response <- Some(request(
////        SendMessage(ChatId(msg.source),
////        "close markup",
////        replyMarkup = Some(ReplyKeyboardRemove()))
//      ))
//    } yield response
//
//    ackFuture.zip(maybeEditFuture.getOrElse(Future.successful(())))
//      .void
//  }
}
