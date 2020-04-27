package projectbot
import java.time.Clock

import com.bot4s.telegram.api.declarative.CommandImplicits
import com.bot4s.telegram.models.{CallbackQuery, User}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import slick.driver.SQLiteDriver.api._
import monix.execution.Scheduler.{global => scheduler}
import projectbot.MyTables.{UsersCourse, UsersLab}
import projectbot.Parsing.Core

import scala.concurrent.duration._

class ScheduleBotCommandsSpec extends FlatSpec  with TestUtils with CommandImplicits with Matchers {
  val coreFile = "Core Courses_Spring_2019-2020 - BS,MS_Spring 2019.csv"
  val electiveFile = "Electives Schedule Spring 2020 Bachelors - Main.csv"
  val electiveLink = "https://docs.google.com/electives"
  val coreLink = "https://docs.google.com/cores"
  val db = Database.forConfig("bot")
  MyTables.createTables(db)
  MyTables.fillDatabase(db, coreFile)
  val bot = new ScheduleBot("token", coreLink, electiveLink, coreFile, electiveFile, db)(scheduler) (Clock.systemDefaultZone)

  it should "change group correctly" in {
    val insert = db.run(bot.courses.insertOrUpdate(UsersCourse(123,"data structure and algorithms", false)))
    Await.result(insert, 3.second)
    val insertLab = db.run(bot.userLabs.insertOrUpdate(UsersLab(123, "data structure and algorithms", Some("B19-01"))))
    Await.result(insertLab, 3.second)
    bot.receiveExtMessage((textMessage("/set_subject_group B19-03 data structure and algorithms"), Some(botUser)))
    val indb = db.run(bot.userLabs.filter(x=>x.id===123 && x.lab_id==="data structure and algorithms").map(e=>e.group_id).result)
    Await.result(indb, 3.second)
    val res = indb.value.get.get
    (res.length, res.head.get) shouldBe (1, "B19-03")
    bot.deleteAllReminders()
  }

  it should "change group only for users who has this class" in {
    bot.receiveExtMessage((textMessage("/set_subject_group M19-DS-01 advanced information retrieval"), Some(botUser)))
    val indb = db.run(bot.userLabs.filter(x=>x.id===123 && x.lab_id==="advanced information retrieval").map(e=>e.group_id).result)
    Await.result(indb, 3.second)
    val res = indb.value.get.get
    res.length shouldBe 0
  }

  it should "change group only for courses with labs" in {
    val insert = db.run(bot.courses.insertOrUpdate(UsersCourse(123,"philosophy", false)))
    Await.result(insert, 3.second)
    bot.receiveExtMessage((textMessage("/set_subject_group B16-SE-01 philosophy"), Some(botUser)))
    val indb = db.run(bot.userLabs.filter(x=>x.id===123 && x.lab_id==="philosophy").map(e=>e.group_id).result)
    Await.result(indb, 3.second)
    val res = indb.value.get.get
    res.length shouldBe 0
  }

  it should "set cores correctly" in {
    bot.receiveExtMessage((textMessage("/set_core M19-DS-01 advanced information retrieval"), Some(botUser)))
    val indb = db.run(bot.userLabs.filter(x=>x.id===123 && x.lab_id==="advanced information retrieval").map(e=>e.group_id).result)
    Await.result(indb, 3.second)
    val res = indb.value.get.get
    (res.length, res.head.get) shouldBe (1, "M19-DS-01")
    bot.deleteAllReminders()
  }

  it should "turn_off_notification correctly for existing notifications for cores" in {
    bot.setupCore(Core("monday", "14:10-15:40", "data structure and algorithms", "adil khan", 108, Some("monday"),
      Some("15:45-17:15"), Some("luiz araújo"), Some(108)), "data structure and algorithms", 123)
    bot.receiveExtMessage((textMessage("/turn_off_notification data structure and algorithms"), Some(botUser)))
    bot.usersSchedulers.count(e=>e._1==123 && e._2=="data structure and algorithms") shouldBe 0
  }

  it should "turn_off_notification correctly for non-existing notifications for cores" in {
    bot.receiveExtMessage((textMessage("/turn_off_notification data structure and algorithms"), Some(botUser)))
    bot.usersSchedulers.count(e=>e._1==123 && e._2=="data structure and algorithms") shouldBe 0
  }

  it should "turn_off_notification correctly for existing notifications for electives" in {
    bot.setupElective("functional programming and scala language",123)
    bot.receiveExtMessage((textMessage("/turn_off_notification programming and scala language"), Some(botUser)))
    bot.usersSchedulers.count(e=>e._1==123 && e._2=="functional programming and scala language") shouldBe 0
  }

  it should "turn_off_notification correctly for non-existing notifications for electives" in {
    bot.receiveExtMessage((textMessage("/turn_off_notification programming and scala language"), Some(botUser)))
    bot.usersSchedulers.count(e=>e._1==123 && e._2=="functional programming and scala language") shouldBe 0
  }

  it should "delete_subject correctly for existing notifications for cores" in {
    bot.setupCore(Core("monday", "14:10-15:40", "data structure and algorithms", "adil khan", 108, Some("monday"),
      Some("15:45-17:15"), Some("luiz araújo"), Some(108)), "data structure and algorithms", 123)
    bot.receiveExtMessage((textMessage("/delete_subject data structure and algorithms"), Some(botUser)))
    val c = bot.usersSchedulers.count(e=>e._1==123 && e._2=="data structure and algorithms")
    val indb = db.run(bot.courses.filter(x=>x.id===123 && x.class_id==="data structure and algorithms").result)
    Await.result(indb, 1.second)
    (c,indb.value.get.get.length) shouldBe (0, 0)
  }

  it should "delete_subject correctly for non-existing notifications for cores" in {
    bot.receiveExtMessage((textMessage("/delete_subject data structure and algorithms"), Some(botUser)))
    val c = bot.usersSchedulers.count(e=>e._1==123 && e._2=="data structure and algorithms")
    val indb = db.run(bot.courses.filter(x=>x.id===123 && x.class_id==="data structure and algorithms").result)
    Await.result(indb, 1.second)
    (c,indb.value.get.get.length) shouldBe (0, 0)
  }

  it should "delete_subject correctly for existing notifications for electives" in {
//    we insert course manually because setupElective sets only schedulers
    val todb = db.run(bot.courses.insertOrUpdate(UsersCourse(123, "functional programming and scala language", true)))
    Await.result(todb, 1.second)
    bot.setupElective("functional programming and scala language", 123)
    bot.receiveExtMessage((textMessage("/delete_subject functional programming and scala language"), Some(botUser)))
    val c = bot.usersSchedulers.count(e=>e._1==123 && e._2=="functional programming and scala language")
    val indb = db.run(bot.courses.filter(x=>x.id===123 && x.class_id==="functional programming and scala language").result)
    Await.result(indb, 1.second)
    (c,indb.value.get.get.length) shouldBe (0, 0)

  }

  it should "delete_subject correctly for non-existing notifications for electives" in {
    bot.receiveExtMessage((textMessage("/delete_subject functional programming and scala language"), Some(botUser)))
    val c = bot.usersSchedulers.count(e=>e._1==123 && e._2=="functional programming and scala language")
    val indb = db.run(bot.courses.filter(x=>x.id===123 && x.class_id==="functional programming and scala language").result)
    Await.result(indb, 1.second)
    (c,indb.value.get.get.length) shouldBe (0, 0)
  }

  it should "handle callbacks for set-group correctly" in {
    val cbq = CallbackQuery(id = "1285522135998800610",
      from = User(id=111, isBot = false, firstName = "cat", lastName = None, username = None, languageCode = None),
      message=None, inlineMessageId=None,
      chatInstance = "8722516864877688585",
      data=Some("B19-03"), gameShortName=None )
    bot.receiveCallbackQuery(cbq)
    val indb = db.run(bot.courses.filter(x=>x.id===111 && !x.is_elective).map(x=>x.class_id).result)
    Await.result(indb, 1.second)
    val inUsers = db.run(bot.users.filter(x=>x.id===111).map(x=>x.group_id).result)
    Await.result(inUsers, 1.second)
    val inLabs = db.run(bot.userLabs.filter(x=>x.id===111).result)
    Await.result(inLabs, 1.second)
    val expectedLab = Vector(UsersLab(111,"analytical geometry and linear algebra 2",Some("B19-03")),
      UsersLab(111,"data structure and algorithms",Some("B19-03")), UsersLab(111,"english (5)",Some("B19-03")),
      UsersLab(111,"introduction to programming 2",Some("B19-03")), UsersLab(111,"mathematical analysis 2",Some("B19-03")),
      UsersLab(111,"theoretical computer science",Some("B19-03")))
    val expectedGroup = Vector("B19-03")
    val expectedCores = Vector("analytical geometry and linear algebra 2", "data structure and algorithms",
      "introduction to programming 2", "mathematical analysis 2", "theoretical computer science")
    (indb.value.get.get, inLabs.value.get.get, inUsers.value.get.get) shouldBe (expectedCores
      , expectedLab, expectedGroup)
    bot.deleteAllReminders()
  }

  it should "handle callbacks for change-group correctly" in {
    val cbq = CallbackQuery(id = "1285522135998800610",
      from = User(id=111, isBot = false, firstName = "cat", lastName = None, username = None, languageCode = None),
      message=None, inlineMessageId=None,
      chatInstance = "8722516864877688585",
      data=Some("updated-b18-03"), gameShortName=None )
    bot.receiveCallbackQuery(cbq)
    val indb = db.run(bot.courses.filter(x=>x.id===111 && !x.is_elective).map(x=>x.class_id).result)
    Await.result(indb, 1.second)
    val inUsers = db.run(bot.users.filter(x=>x.id===111).map(x=>x.group_id).result)
    Await.result(inUsers, 1.second)
    val inLabs = db.run(bot.userLabs.filter(x=>x.id===111).result)
    Await.result(inLabs, 1.second)
    val expectedLab = Vector(UsersLab(111,"control theory",Some("B18-03")), UsersLab(111,"data modeling and databases 2",
      Some("B18-03")), UsersLab(111,"introduction to ai",Some("B18-03")), UsersLab(111,"networks",Some("B18-03")),
      UsersLab(111,"probability and statistics",Some("B18-03")), UsersLab(111,"software project",Some("B18-03")))
    val expectedGroup = Vector("B18-03")
    val expectedCores = Vector("control theory", "data modeling and databases 2", "introduction to ai", "networks",
      "probability and statistics", "software project")
    (indb.value.get.get, inLabs.value.get.get, inUsers.value.get.get) shouldBe (expectedCores
      , expectedLab, expectedGroup)
    bot.deleteAllReminders()
  }

  it should "handle callbacks for change-group correctly even if user did not existed before" in {
    val cbq = CallbackQuery(id = "1285522135998800610",
      from = User(id=1000, isBot = false, firstName = "cat", lastName = None, username = None, languageCode = None),
      message=None, inlineMessageId=None,
      chatInstance = "8722516864877688585",
      data=Some("updated-b18-03"), gameShortName=None )
    bot.receiveCallbackQuery(cbq)
    val indb = db.run(bot.courses.filter(x=>x.id===1000 && !x.is_elective).map(x=>x.class_id).result)
    Await.result(indb, 1.second)
    val inUsers = db.run(bot.users.filter(x=>x.id===1000).map(x=>x.group_id).result)
    Await.result(inUsers, 1.second)
    val inLabs = db.run(bot.userLabs.filter(x=>x.id===1000).result)
    Await.result(inLabs, 1.second)
    val expectedLab = Vector(UsersLab(1000,"control theory",Some("B18-03")), UsersLab(1000,"data modeling and databases 2",
      Some("B18-03")), UsersLab(1000,"introduction to ai",Some("B18-03")), UsersLab(1000,"networks",Some("B18-03")),
      UsersLab(1000,"probability and statistics",Some("B18-03")), UsersLab(1000,"software project",Some("B18-03")))
    val expectedGroup = Vector("B18-03")
    val expectedCores = Vector("control theory", "data modeling and databases 2", "introduction to ai", "networks",
      "probability and statistics", "software project")
    (indb.value.get.get, inLabs.value.get.get, inUsers.value.get.get) shouldBe (expectedCores, expectedLab, expectedGroup)
    bot.deleteAllReminders()
  }

  it should "handle callbacks for set-elective correctly" in {
    val cbq = CallbackQuery(id = "1285522135998800610",
      from = User(id=1000, isBot = false, firstName = "cat", lastName = None, username = None, languageCode = None),
      message=None, inlineMessageId=None,
      chatInstance = "8722516864877688585",
      data=Some("advanced agile software design"), gameShortName=None )
    bot.receiveCallbackQuery(cbq)
    val indb = db.run(bot.courses.filter(x=>x.id===1000 && x.is_elective).result)
    Await.result(indb, 1.second)
    indb.value.get.get shouldBe Vector(UsersCourse(1000, "advanced agile software design", true))
    bot.deleteAllReminders()
  }

  it should "ignore incorrect callbacks" in {
    val cbq = CallbackQuery(id = "1285522135998800610",
      from = User(id=5000, isBot = false, firstName = "cat", lastName = None, username = None, languageCode = None),
      message=None, inlineMessageId=None,
      chatInstance = "8722516864877688585",
      data=Some("cats-top"), gameShortName=None )
    bot.receiveCallbackQuery(cbq)
    bot.deleteAllReminders()
  }

}
