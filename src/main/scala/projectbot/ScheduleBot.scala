package projectbot

import java.time.Clock

import projectbot.Time4Bot._
import com.bot4s.telegram.api.declarative.{Callbacks, Commands}
import cats.instances.future._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.EditMessageReplyMarkup
import com.bot4s.telegram.models.{CallbackQuery, ChatId, InlineKeyboardButton, InlineKeyboardMarkup, Message}
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.concurrent.{Await, Future}
import cats.syntax.functor._
import monix.execution.{Cancelable, Scheduler}
import projectbot.MyTables.{CommonClasses, Labs, User, UserCourse, UserLab, Users, UsersCourse, UsersLab}
//import slick.driver.SQLiteDriver.api._

import slick.driver.PostgresDriver.api._
import com.bot4s.telegram.methods._
import com.bot4s.telegram.models.ChatId.Chat
import projectbot.Parsing.Core

import scala.concurrent.duration._
import scala.util.Failure
import scala.util.control.Breaks.{break, breakable}

/** Generates random values.
 */
class ScheduleBot(val token: String,
                  val coreLink: String,
                  val electiveLink: String,
                  val coreFile: String,
                  val electiveFile: String,
                  db: Database)(scheduler: Scheduler)(clock: Clock) extends TelegramBot
  with Polling
  with Commands[Future]
  with Callbacks[Future] {

  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.TRACE


  def createGroupButtons(): Seq[InlineKeyboardButton] = {
    Parsing.groups(coreFile).map(e => InlineKeyboardButton.callbackData(e, e))
  }

  def createElectiveButtons(): Seq[InlineKeyboardButton] = {
    Parsing.listOfElectives(electiveFile).map(e => InlineKeyboardButton.callbackData(e.toLowerCase, e.toLowerCase))
  }

  def createGroupUpdateButtons(): Seq[InlineKeyboardButton] = {
    Parsing.groups(coreFile).map(e => InlineKeyboardButton.callbackData(e, "updated-" + e.toLowerCase()))
  }

  val groupButtons: Seq[InlineKeyboardButton] = createGroupButtons()
  val electiveButtons: Seq[InlineKeyboardButton] = createElectiveButtons()
  val updatedGroupButton: Seq[InlineKeyboardButton] = createGroupUpdateButtons()
  val groupTags: Seq[String] = Parsing.groups(coreFile)
  val electiveTags: Seq[String] = Parsing.listOfElectives(electiveFile).map(e => e.toLowerCase)
  val updatedGroupTags: Seq[String] = Parsing.groups(coreFile).map(e => "updated-" + e.toLowerCase())

  val labs = TableQuery[Labs]
  val cores = TableQuery[CommonClasses]
  val users = TableQuery[Users]
  val courses = TableQuery[UserCourse]
  val userLabs = TableQuery[UserLab]
  //(user_id, course_name, scheduler) - to cancell notifications if course id finished our changed
  var usersSchedulers = List.empty: List[(Int, String, Cancelable)]
  val listOfTuples = Parsing.listOfElectives(electiveFile).map(e => (e.toLowerCase, Parsing.parseElectives(e)))
  val electivesMap = listOfTuples.toMap
  //(user_id, course_name, [(scheduler 1 day before, scheduler 10 min before)])
  // - to cancell notifications if course id finished our changed
  var usersElectiveSchedulers = List.empty: List[(Int, String, List[(Cancelable, Cancelable)])]

  // Or just the scalaj-http backend
  override val client: RequestHandler[Future] = new ScalajHttpClient(token)

  val rng = new scala.util.Random(System.currentTimeMillis())

  def classesOnTheDay(day: String, res: Seq[String]): (List[(String, String, String, Int)],
    List[(String, Option[String], Option[String], Option[Int])],
    List[(String, String, String, Int)]) = {

    var todayLectures = List.empty: List[(String, String, String, Int)]
    var todayTutorials = List.empty: List[(String, Option[String], Option[String], Option[Int])]
    var todayLabs = List.empty: List[(String, String, String, Int)]
    for (cl <- res) {
      val hasLect = db.run(cores.filter(x => x.classId === cl && x.dayLecture === day)
        .map(e => (e.classId, e.prof, e.lectureTime, e.lectureRoom)).result)
      Await.result(hasLect, 5000.millis)
      if (hasLect.value.get.get.nonEmpty) todayLectures = todayLectures ++ List(hasLect.value.get.get.head)

      val hasTut = db.run(cores.filter(x => x.classId === cl
        && !x.dayTutorial.isEmpty
        && !x.tutorialTeacher.isEmpty
        && !x.tutorialTime.isEmpty
        && !x.tutorialRoom.isEmpty
        && x.dayTutorial === day)
        .map(e => (e.classId, e.tutorialTeacher, e.tutorialTime, e.tutorialRoom)).result)
      Await.result(hasTut, 5000.millis)
      if (hasTut.value.get.get.nonEmpty) todayTutorials = todayTutorials ++ List(hasTut.value.get.get.head)

      val haslab = db.run(labs.filter(x => x.classId === cl && x.labDay === day)
        .map(e => (e.classId, e.ta, e.labTime, e.labRoom)).result)
      Await.result(haslab, 5000.millis)
      if (haslab.value.get.get.nonEmpty) todayLabs = todayLabs ++ List(haslab.value.get.get.head)
    }
    (todayLectures, todayTutorials, todayLabs)
  }

  //todo first Bachelor or masters, than year than group filtered
  onCommand("set_group") {
    implicit msg =>
      reply("Select your group number",
        replyMarkup = Some(InlineKeyboardMarkup
          .singleColumn(groupButtons)
        )
      ).void
  }

  onCommand("set_elective") {
    implicit msg =>
      reply("Select your electives",
        replyMarkup = Some(InlineKeyboardMarkup
          .singleColumn(electiveButtons)
        )
      ).void
  }

  onCommand("core_link") {
    implicit msg =>
      reply(s"Core courses table is available here $coreLink").void
  }

  onCommand("electives_link") {
    implicit msg =>
      reply(s"Elective courses table is available here $electiveLink").void
  }

  onCommand("my_courses") {
    implicit msg =>
      val theirCourses = courses.filter(_.id === msg.from.get.id).map(e => e.class_id).result
      db.run(theirCourses).flatMap { res =>
        if (res.nonEmpty) reply(s"Your courses \n ${res.mkString("\n")}")
        else reply(s"You do not have any courses yet")

      }.void
  }

  onCommand("change_group") {
    implicit msg =>
      reply("Select your new group",
        replyMarkup = Some(InlineKeyboardMarkup
          .singleColumn(updatedGroupButton)
        )
      ).void
  }

  onCommand("day") { implicit msg =>
    withArgs { args =>
      if (args.isEmpty) reply("Please enter the day you are interested in", replyMarkup = None).void
      else {
        val day = args.head.toLowerCase()
        val theirCourses = courses.filter(_.id === msg.from.get.id).map(e => e.class_id).result
        db.run(theirCourses).flatMap { res =>
          val afterSearch = classesOnTheDay(day, res)
          val todayLectures = afterSearch._1
          val todayTutorials = afterSearch._2
          val todayLabs = afterSearch._3
          if (todayLectures.isEmpty && todayTutorials.isEmpty && todayLabs.isEmpty) reply(s"Your have no classes on ${day}")
          else {
            val toPrint = s"Lectures \n ${todayLectures.mkString("\n")} \n Tutorials  \n ${todayTutorials.mkString("\n")}" +
              s"\n Labs \n  \n ${todayLabs.mkString("\n")}"
            reply(s"Your courses for $day are \n $toPrint")
          }

        }.void
      }
    }
  }

  //  argument is group + the name of the subject
  onCommand("set_subject_group") { implicit msg =>
    withArgs { args =>
      if (args.isEmpty) reply("Please enter the subject you would like to set group to", replyMarkup = None).void
      else {
        val group = args.head.toUpperCase()
        if (!groupTags.contains(group)) reply(s"$group is invalid group. Use 'groups' to see the list of all groups ").void
        else {
          val subject = args.take(args.length).drop(1).mkString(" ").toLowerCase()
          db.run(courses.filter(x => x.id === msg.from.get.id && x.class_id === subject).result).flatMap { isInLectures =>
            db.run(userLabs.filter(x => x.id === msg.from.get.id && x.lab_id === subject).result).flatMap { isInLabs =>
              if (isInLabs.isEmpty && isInLectures.isEmpty) {
                reply(s"You do not have this subject, If you want to add it, please, use 'set_core' command " +
                  s"or check 'my_courses' to check if you have entered the course name correctly")
              }
              else {
                if (isInLabs.isEmpty && isInLectures.nonEmpty) {
                  reply(s"You have only lectures (and maybe tutorials) this subject, hence, group does not matter")
                }
                else {
                  db.run(DBIO.seq(userLabs.insertOrUpdate(UsersLab(msg.from.get.id, subject, Some(group))))).flatMap { _ =>
                    val reminder = usersSchedulers.filter(x => x._1 == msg.from.get.id && x._2.contains(subject + "-lab"))
                    if (reminder.nonEmpty) {
                      reminder.head._3.cancel()
                      usersSchedulers = usersSchedulers diff List(reminder.head)
                    }
                    reply(s"You have successfully changed group for $subject to $group")
                  }
                }
              }
            }
          }.void
        }
      }
    }
  }

  onCommand("groups") { implicit msg =>
    reply(s"List of all possible groups : \n ${groupTags.mkString("\n")}").void
  }

  onCommand("groups_for") { implicit msg =>
    withArgs { args =>
      if (args.isEmpty) reply("Please enter the subject you would like to see groups to", replyMarkup = None).void
      else {
        val subject = args.take(args.length).mkString(" ").toLowerCase()
        db.run(labs.filter(x => x.classId === subject && !x.groupId.isEmpty).map(x => x.groupId).result).flatMap { gr =>
          if (gr.nonEmpty) {
            reply(s"Groups are \n${gr.map(e => e.get).mkString("\n")}")
          }
          else reply("There are no labs for this subject, hence, groups does not matter. If you want to see more info," +
            " go directly to timetable using 'core_link' ", replyMarkup = None)
        }.void
      }
    }
  }

  onCommand("set_core") { implicit msg =>
    withArgs { args =>
      if (args.isEmpty) reply("Please enter the subject you would like to set group to", replyMarkup = None).void
      else {
        val group = args.head.toUpperCase()
        if (!groupTags.contains(group)) reply(s"$group is invalid group. Use 'groups' to see the list of all groups ").void
        else {
          val subject = args.take(args.length).drop(1).mkString(" ").toLowerCase()
          db.run(cores.filter(_.classId === subject).exists.result).flatMap { isReal =>
            db.run(labs.filter(x => x.classId === subject && !x.groupId.isEmpty && x.groupId === group).
              exists.result).flatMap { isInLabs =>
              if (!isReal && !isInLabs) reply(s"There is no such a core course").void
              else {
                if (isReal) {
                  val ifReal = db.run(cores.filter(_.classId === subject).result).flatMap {
                    ifReal => Future(setupCore(ifReal.head, subject, msg.from.get.id))
                  }
                }
                if (isInLabs) {
                  val ifInLabs = db.run(labs.filter(x => x.classId === subject && !x.groupId.isEmpty && x.groupId === group)
                    .result).flatMap { ifInLabs =>
                    val c = setupLab(ifInLabs.head, msg.from.get.id)
                    usersSchedulers = usersSchedulers :+ (msg.from.get.id, ifInLabs.head + "-lab", c)
                    Future("lab scheduler added")
                  }
                  val insert = db.run(userLabs.insertOrUpdate(UsersLab(msg.from.get.id, subject, Some(group)))).flatMap {
                    _ => Future("lab inserted")
                  }
                }
                reply(s"Course $subject is successfully added for group $group")
              }
            }
          }.void
        }
      }
    }
  }

  //  will not show if there were no notifications
  onCommand("turn_off_notification") { implicit msg =>
    withArgs { args =>
      if (args.isEmpty) reply("Please enter the subject", replyMarkup = None).void
      else {
        val subject = args.take(args.length).mkString(" ").toLowerCase()
        val isIn = usersSchedulers.filter(x => x._1 == msg.from.get.id && x._2.contains(subject))
        if (isIn.nonEmpty) {
          usersSchedulers.filter(x => x._1 == msg.from.get.id && x._2.contains(subject)).map(x => x._3.cancel())
          usersSchedulers = usersSchedulers diff isIn
        }
        isIn.map(x => x._3.cancel())
        val isInElectives = usersElectiveSchedulers.filter(x => x._1 == msg.from.get.id && x._2.contains(subject)): List[(Int, String, List[(Cancelable, Cancelable)])]
        if (isIn.nonEmpty) {
          usersElectiveSchedulers.filter(x => x._1 == msg.from.get.id && x._2.contains(subject)).flatMap(x => x._3).foreach(x => {
            x._1.cancel();
            x._2.cancel()
          })
          usersElectiveSchedulers = usersElectiveSchedulers diff isInElectives
        }
        isInElectives.flatMap(x => x._3).map(x => {
          x._1.cancel();
          x._2.cancel()
        })
        reply(s"Notifications for $subject are successfully turned off").void
      }
    }
  }

  // it will not say if you did not have this subject
  onCommand("delete_subject") { implicit msg =>
    withArgs { args =>
      if (args.isEmpty) reply("Please enter the subject", replyMarkup = None).void
      else {
        val subject = args.take(args.length).mkString(" ").toLowerCase()
        val checkInCores = usersSchedulers.map(e => e._2).contains(subject)
        if (checkInCores) {
          val isIn = usersSchedulers.filter(x => x._1 == msg.from.get.id && x._2.contains(subject))
          if (isIn.nonEmpty) usersSchedulers = usersSchedulers diff isIn;
          isIn.map(x => x._3.cancel())
        }
        val checkInElectives = usersElectiveSchedulers.map(e => e._2).contains(subject)
        if (checkInElectives) {
          deleteElective(subject, msg.from.get.id)
        }
        db.run(userLabs.filter(x => x.lab_id === subject && x.id === msg.from.get.id).exists.result).flatMap { ifhasLabs =>
          if (ifhasLabs.value) {
            db.run(userLabs.filter(x => x.lab_id === subject && x.id === msg.from.get.id).delete).flatMap { _ => Future("labs successfully deleted") }
          }
          db.run(courses.filter(x => x.class_id === subject && x.id === msg.from.get.id).exists.result).flatMap { ifHasCores =>
            if (ifHasCores.value) {
              db.run(courses.filter(x => x.class_id === subject && x.id === msg.from.get.id).delete).flatMap { _ => Future("cores successfully deleted") }
            }
            Future("cores successfully deleted")
          }
          reply("The subject was successfully deleted")
        }.void
      }
    }
  }

  onCommand("today") {
    implicit msg =>
      val theirCourses = courses.filter(_.id === msg.from.get.id).map(e => e.class_id).result
      db.run(theirCourses).flatMap { res =>
        val dayOfWeek = todayDay(clock).toString.toLowerCase
        val afterSearch = classesOnTheDay(dayOfWeek, res)
        val todayLectures = afterSearch._1
        val todayTutorials = afterSearch._2
        val todayLabs = afterSearch._3
        if (todayLectures.isEmpty && todayTutorials.isEmpty && todayLabs.isEmpty) reply(s"Your have no classes today")
        else {
          val toPrint = s"Lectures \n ${todayLectures.mkString("\n")} \n Tutorials  \n ${todayTutorials.mkString("\n")}" +
            s"\n Labs  \n ${todayLabs.mkString("\n")}"
          reply(s"Your courses for today are \n $toPrint")
        }

      }.void
  }


  def hasGroupTag(cbq: CallbackQuery, group: Seq[String]): Option[String] = {
    (for (gr <- group.toStream if cbq.data.exists(_.startsWith(gr))) yield gr).headOption
  }

  def emptyMarkup(cbq: CallbackQuery): Option[Future[Either[Boolean, Message]]] = {
    val maybeEditFuture = for {
      msg <- cbq.message
      response <- Some(request(
        EditMessageReplyMarkup(
          Some(ChatId(msg.source)), // msg.chat.id
          Some(msg.messageId),
          replyMarkup = None)
      ))
    } yield response
    maybeEditFuture
  }

  //  for each elective there will be 2 notifications -a day before and 10 min before a class
  //  we assume that  here elective name is aleady checked
  def setupElective(electiveName: String, userId: Int): Unit = {
    val values = electivesMap.get(electiveName).get
    var res = List.empty: List[(Cancelable, Cancelable)]
    for (v <- values) {
      breakable {
        val timeBefore = timeBeforeElective(v.date, v.time, clock)
        timeBefore match {
          case Some(value) =>
            val beforeDay = scheduler.scheduleOnce((value - 24 * 60).minutes) {
              request(SendMessage(Chat(userId), s"You will have $electiveName in tomorrow " +
                s"(${v.weekday}, ${v.date}) with ${v.teacher} in ${v.room}"))
            }
            val beforeClass = scheduler.scheduleOnce((value).minutes) {
              request(SendMessage(Chat(userId), s"You will have $electiveName in 10 minutes + " +
                s"(${v.weekday}, ${v.date}) with ${v.teacher} in ${v.room}"))
            }
            res = res :+ (beforeDay, beforeClass)
          case None => break
        }
      }
    }
    usersElectiveSchedulers = usersElectiveSchedulers :+ (userId, electiveName, res)
  }

  def setupLab(e: (Parsing.Labs, Option[String]), fromId: Int): Cancelable = {
    scheduler.scheduleWithFixedDelay(timeTill(e._1.weekday, e._1.time.substring(0, 5), clock).minutes, 7.days) {
      request(SendMessage(Chat(fromId), s"Your lab from ${e._1.ta} on ${e._1.courseName} will be " +
        s"in 10 minutes (${e._1.time}) at room ${e._1.room}"))
    }
  }

  def setupCore(res: Core, course: String, fromId: Int): Unit = {
    val c = scheduler.scheduleWithFixedDelay(timeTill(res.lecDay, res.lecTime.substring(0, 5), clock).minutes, 7.days) {
      request(SendMessage(Chat(fromId), s"Your lecture from ${res.lecturer} on $course will be " +
        s"in 10 minutes (${res.lecTime}) at room ${res.lecRoom}"))
    }
    usersSchedulers = usersSchedulers :+ (fromId, course + "-lec", c)
    if (res.tutDay.nonEmpty) {
      val c = scheduler.scheduleWithFixedDelay(timeTill(res.tutDay.get, res.tutTime.get.substring(0, 5), clock).minutes, 7.days) {
        request(SendMessage(Chat(fromId), s"Your tutorial from ${res.tutTeacher} on $course will be " +
          s"in 10 minutes (${res.tutTime}) at room ${res.tutRoom}"))
      }
      usersSchedulers = usersSchedulers :+ (fromId, course + "-tut", c)
    }
  }

  def setupCoursesAndLabs(value: String, cbq: CallbackQuery): Future[List[Int]] = {
    val theirLabs = labs.filter(_.groupId === value).result
    db.run(theirLabs).flatMap { res =>
      for (e <- res) {
        val c = setupLab(e, cbq.from.id)
        usersSchedulers = usersSchedulers :+ (cbq.from.id, e._1.courseName + "-lab", c)
      }
      val labsinOneGo = DBIO.sequence(res.map(el => userLabs.insertOrUpdate(UsersLab(cbq.from.id, el._1.courseName, Some(value)))))
      //      todo ??? after db.run?
      db.run(labsinOneGo).flatMap(_=>Future("success"))
      //           res.foreach{e => {
      //      val c = setupLab(e, cbq.from.id)
      //      usersSchedulers = usersSchedulers :+ (cbq.from.id, e._1.courseName + "-lab", c)
      //    }}
    }
    Parsing.groupSubjects(coreFile, value) match {
      case Right(v) =>
        val unique = Parsing.uniqueCourses(v, false)
        unique.foreach { e =>
          db.run(cores.filter(_.classId === e).result).flatMap(res => Future(setupCore(res.head, e, cbq.from.id)))
        }
        val toInsert = unique.map(u => courses.insertOrUpdate(UsersCourse(cbq.from.id, u, false)))
        db.run(DBIO.sequence(toInsert))
      case _ => Future(List(0))
    }
  }

  //  deletes reminders for classes named in the seq
  def deleteReminders(seq: Seq[String], userId: Int): Unit = {
    val usersCourses = usersSchedulers.filter(x => x._1 == userId && seq.contains(x._2.substring(0, x._2.length - 4)))
    for (course <- usersCourses) {
      course._3.cancel()
      usersSchedulers = usersSchedulers diff List(course)
    }
  }

  def deleteAllReminders(): Unit = {
    usersSchedulers.map(el => el._3.cancel())
    usersSchedulers = List.empty: List[(Int, String, Cancelable)]
    usersElectiveSchedulers.map(el => el._3.map(e => {
      e._1.cancel();
      e._2.cancel()
    }))
    usersElectiveSchedulers = List.empty: List[(Int, String, List[(Cancelable, Cancelable)])]
  }


  def deleteElective(electiveName: String, userId: Int): Unit = {
    val elects = usersElectiveSchedulers.filter(e => e._2 == electiveName && e._1 == userId)
    usersElectiveSchedulers.filter(e => e._2 == electiveName && e._1 == userId).flatMap(x => x._3).foreach(x => {
      x._1.cancel();
      x._2.cancel()
    })
    usersElectiveSchedulers = usersElectiveSchedulers diff elects
    elects.flatMap(x => x._3).foreach(x => {
      x._1.cancel();
      x._2.cancel()
    })
  }


  onCallbackQuery { implicit cbq =>
    val res = hasGroupTag(cbq, groupTags)
    res match {
      case Some(value) =>
        val ackFuture = ackCallback(Some(s"${cbq.from.firstName} pressed the button with $value"))(cbq)
        //        one user will not be created twice due to the primary keys
        //        todo return message "use /change_group"
        db.run(DBIO.seq(users += User(cbq.from.id, value))).flatMap(_ => Future("group inserted"))
        setupCoursesAndLabs(value, cbq).onComplete(_ => Future("classes inserted"))
        val fut = emptyMarkup(cbq)
        ackFuture.zip(fut.getOrElse(Future.successful(()))).void

      case _ =>
        val elec = hasGroupTag(cbq, electiveTags)
        elec match {
          case Some(value) =>
            val ackFuture = ackCallback(Some(s"${cbq.from.firstName} pressed the button with ${value}"))(cbq)
            val result = db.run(DBIO.seq(courses += UsersCourse(cbq.from.id, value, true))).flatMap(_ => Future("elective added"))
            setupElective(value, cbq.from.id)
            val fut = emptyMarkup(cbq)
            ackFuture.zip(fut.getOrElse(Future.successful(()))).void

          case _ =>
            val updateGroup = hasGroupTag(cbq, updatedGroupTags)
            updateGroup match {
              case Some(valueDirty) =>
                val value = valueDirty.replace("updated-", "").toUpperCase()
                val ackFuture = ackCallback(Some(s"${cbq.from.firstName} pressed the button with $value"))(cbq)
                //this will delete all labs for the prev group and this person
                db.run(users.filter(_.id === cbq.from.id).map(e => e.group_id).result).flatMap { prevGroup =>
                  if (prevGroup.nonEmpty) {
                    val oldLabs = userLabs.filter(x => (x.id === cbq.from.id
                      &&
                      !x.group_id.isEmpty
                      &&
                      x.group_id === prevGroup.head))
                    db.run(oldLabs.map(e => e.lab_id).result).flatMap { intermediateLabs =>
                      Future(deleteReminders(intermediateLabs, cbq.from.id))
                    }
                    db.run(oldLabs.delete).flatMap{ _ => Future("affected rows deleted") }

                  }
                  Future("there were no prev group")
                }
                val oldCourses = courses.filter(x => x.id === cbq.from.id && !x.is_elective)
                db.run(oldCourses.map(e => e.class_id).result).flatMap { intermediateCores =>
                  Future(deleteReminders(intermediateCores, cbq.from.id))
                }

                db.run(oldCourses.delete).flatMap(_ => Future("updated"))

                db.run(DBIO.seq(users.insertOrUpdate(User(cbq.from.id, value)))).flatMap{ _ => Future("group changed") }


                setupCoursesAndLabs(value, cbq).flatMap(_=> Future("cores setted"))

                val fut = emptyMarkup(cbq)
                ackFuture.zip(fut.getOrElse(Future.successful(()))).void
              case _ =>
                val ackFuture = ackCallback(None)(cbq)
                val fut = emptyMarkup(cbq)
                ackFuture.zip(fut.getOrElse(Future.successful(()))).void
            }
        }
    }
  }
}
