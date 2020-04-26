package projectbot

import java.time.{Clock, Instant, ZoneId}

import monix.execution.schedulers.TestScheduler
import org.scalatest.{FlatSpec, Matchers}
import com.bot4s.telegram.models.{CallbackQuery, InlineKeyboardButton, User}
import monix.execution.Cancelable

import scala.concurrent.duration._
import slick.driver.SQLiteDriver.api._
import monix.execution.Scheduler.{global => scheduler}
import projectbot.MyTables.UsersCourse
import projectbot.Parsing.Core

import scala.concurrent.Await


class ScheduleBotSpec extends FlatSpec with Matchers {
//  val testScheduler = TestScheduler()
//  testScheduler.scheduleOnce(1.second) {
//    println("Delayed execution!")
//  }
//  testScheduler.executionModel
//  empty database for tests
  val db = Database.forConfig("bot")
  MyTables.createTables(db)
  val coreFile = "Core Courses_Spring_2019-2020 - BS,MS_Spring 2019.csv"
  val electiveFile = "Electives Schedule Spring 2020 Bachelors - Main.csv"
  val bot = new ScheduleBot("mew", "coreLink", "electiveLink",
    coreFile, electiveFile, db)(scheduler)(Clock.systemDefaultZone)

  val groupButtons = List(InlineKeyboardButton("B19-01",Some("B19-01"),None,None,None,None,None,None), 
    InlineKeyboardButton("B19-02",Some("B19-02"),None,None,None,None,None,None), 
    InlineKeyboardButton("B19-03",Some("B19-03"),None,None,None,None,None,None), 
    InlineKeyboardButton("B19-04",Some("B19-04"),None,None,None,None,None,None), 
    InlineKeyboardButton("B19-05",Some("B19-05"),None,None,None,None,None,None), 
    InlineKeyboardButton("B19-06",Some("B19-06"),None,None,None,None,None,None), 
    InlineKeyboardButton("B18-01",Some("B18-01"),None,None,None,None,None,None), 
    InlineKeyboardButton("B18-02",Some("B18-02"),None,None,None,None,None,None), 
    InlineKeyboardButton("B18-03",Some("B18-03"),None,None,None,None,None,None), 
    InlineKeyboardButton("B18-04",Some("B18-04"),None,None,None,None,None,None), 
    InlineKeyboardButton("B18-05",Some("B18-05"),None,None,None,None,None,None), 
    InlineKeyboardButton("B18-06",Some("B18-06"),None,None,None,None,None,None), 
    InlineKeyboardButton("B17-DS-01",Some("B17-DS-01"),None,None,None,None,None,None), 
    InlineKeyboardButton("B17-DS-02",Some("B17-DS-02"),None,None,None,None,None,None), 
    InlineKeyboardButton("B17-SE-01",Some("B17-SE-01"),None,None,None,None,None,None), 
    InlineKeyboardButton("B17-SE-02",Some("B17-SE-02"),None,None,None,None,None,None), 
    InlineKeyboardButton("B17-SB-01",Some("B17-SB-01"),None,None,None,None,None,None), 
    InlineKeyboardButton("B17-RO-01",Some("B17-RO-01"),None,None,None,None,None,None), 
    InlineKeyboardButton("B16-SE-01",Some("B16-SE-01"),None,None,None,None,None,None), 
    InlineKeyboardButton("B16-RO-01",Some("B16-RO-01"),None,None,None,None,None,None), 
    InlineKeyboardButton("B16-DS-01",Some("B16-DS-01"),None,None,None,None,None,None), 
    InlineKeyboardButton("B16-DS-02",Some("B16-DS-02"),None,None,None,None,None,None), 
    InlineKeyboardButton("M19-SE-01",Some("M19-SE-01"),None,None,None,None,None,None),
    InlineKeyboardButton("M19-SE-02",Some("M19-SE-02"),None,None,None,None,None,None),
    InlineKeyboardButton("M19-DS-01",Some("M19-DS-01"),None,None,None,None,None,None),
    InlineKeyboardButton("M19-DS-02",Some("M19-DS-02"),None,None,None,None,None,None),
    InlineKeyboardButton("M19-RO-01",Some("M19-RO-01"),None,None,None,None,None,None),
    InlineKeyboardButton("M18-RO-01",Some("M18-RO-01"),None,None,None,None,None,None))

  val electiveButtons = List(InlineKeyboardButton("natural language processing and machine learning",
    Some("natural language processing and machine learning"),None,None,None,None,None,None),
    InlineKeyboardButton("computer graphics in game development",
      Some("computer graphics in game development"),None,None,None,None,None,None),
    InlineKeyboardButton("advanced agile software design",
      Some("advanced agile software design"),None,None,None,None,None,None),
    InlineKeyboardButton("modern application production",
      Some("modern application production"),None,None,None,None,None,None),
    InlineKeyboardButton("design patterns",Some("design patterns"),None,None,None,None,None,None),
    InlineKeyboardButton("economics of entrepreneurship in it industry",
      Some("economics of entrepreneurship in it industry"),None,None,None,None,None,None),
    InlineKeyboardButton("enterprise programming on javascript - advanced",
      Some("enterprise programming on javascript - advanced"),None,None,None,None,None,None),
    InlineKeyboardButton("total virtualization",Some("total virtualization"),None,None,None,None,None,None),
    InlineKeyboardButton("product\'s highly loaded architecture",
      Some("product's highly loaded architecture"),None,None,None,None,None,None),
    InlineKeyboardButton("devops",Some("devops"),None,None,None,None,None,None),
    InlineKeyboardButton("human computer interaction design for ai",
      Some("human computer interaction design for ai"),None,None,None,None,None,None),
    InlineKeyboardButton("technical writing and communication",
      Some("technical writing and communication"),None,None,None,None,None,None),
    InlineKeyboardButton("introduction to  public speaking for it-specialist",
      Some("introduction to  public speaking for it-specialist"),None,None,None,None,None,None),
    InlineKeyboardButton("advanced topics in software testing",
      Some("advanced topics in software testing"),None,None,None,None,None,None),
    InlineKeyboardButton("critical thinking for it-specialist",
      Some("critical thinking for it-specialist"),None,None,None,None,None,None),
    InlineKeyboardButton("reading skills  for it-specialist",
      Some("reading skills  for it-specialist"),None,None,None,None,None,None),
    InlineKeyboardButton("introduction to it entrepreneurship",
      Some("introduction to it entrepreneurship"),None,None,None,None,None,None),
    InlineKeyboardButton("programming windows services with c++",
      Some("programming windows services with c++"),None,None,None,None,None,None),
    InlineKeyboardButton("advanced academic research- writing and performance",
      Some("advanced academic research- writing and performance"),None,None,None,None,None,None),
    InlineKeyboardButton("volunteer and crowd-based approaches in computing",
      Some("volunteer and crowd-based approaches in computing"),None,None,None,None,None,None),
    InlineKeyboardButton("design fiction",Some("design fiction"),None,None,None,None,None,None),
    InlineKeyboardButton("tech startup design",Some("tech startup design"),None,None,None,None,None,None),
    InlineKeyboardButton("business-track",Some("business-track"),None,None,None,None,None,None),
    InlineKeyboardButton("personal efficiency skills of it-specialist",
      Some("personal efficiency skills of it-specialist"),None,None,None,None,None,None),
    InlineKeyboardButton("programming in haskell",Some("programming in haskell"),None,None,None,None,None,None),
    InlineKeyboardButton("consensus theory and concurrent programming on a shared memory",
      Some("consensus theory and concurrent programming on a shared memory"),None,None,None,None,None,None),
    InlineKeyboardButton("functional programming and scala language",
      Some("functional programming and scala language"),None,None,None,None,None,None),
    InlineKeyboardButton("practical artificial intelligence",Some("practical artificial intelligence"),None,None,None,None,None,None),
    InlineKeyboardButton("introduction to career development for it-specialist",
      Some("introduction to career development for it-specialist"),None,None,None,None,None,None),
    InlineKeyboardButton("mobile development using qt",Some("mobile development using qt"),None,None,None,None,None,None),
    InlineKeyboardButton("psychology of it-specialist",Some("psychology of it-specialist"),None,None,None,None,None,None),
    InlineKeyboardButton("russian as a foreign language",Some("russian as a foreign language"),None,None,None,None,None,None),
    InlineKeyboardButton("software requirements and specifications",Some("software requirements and specifications"),None,None,None,None,None,None))
  
  val updatedGroupButtons = List(InlineKeyboardButton("B19-01",Some("updated-b19-01"),None,None,None,None,None,None),
    InlineKeyboardButton("B19-02",Some("updated-b19-02"),None,None,None,None,None,None),
    InlineKeyboardButton("B19-03",Some("updated-b19-03"),None,None,None,None,None,None),
    InlineKeyboardButton("B19-04",Some("updated-b19-04"),None,None,None,None,None,None),
    InlineKeyboardButton("B19-05",Some("updated-b19-05"),None,None,None,None,None,None),
    InlineKeyboardButton("B19-06",Some("updated-b19-06"),None,None,None,None,None,None),
    InlineKeyboardButton("B18-01",Some("updated-b18-01"),None,None,None,None,None,None),
    InlineKeyboardButton("B18-02",Some("updated-b18-02"),None,None,None,None,None,None),
    InlineKeyboardButton("B18-03",Some("updated-b18-03"),None,None,None,None,None,None),
    InlineKeyboardButton("B18-04",Some("updated-b18-04"),None,None,None,None,None,None),
    InlineKeyboardButton("B18-05",Some("updated-b18-05"),None,None,None,None,None,None),
    InlineKeyboardButton("B18-06",Some("updated-b18-06"),None,None,None,None,None,None),
    InlineKeyboardButton("B17-DS-01",Some("updated-b17-ds-01"),None,None,None,None,None,None),
    InlineKeyboardButton("B17-DS-02",Some("updated-b17-ds-02"),None,None,None,None,None,None),
    InlineKeyboardButton("B17-SE-01",Some("updated-b17-se-01"),None,None,None,None,None,None),
    InlineKeyboardButton("B17-SE-02",Some("updated-b17-se-02"),None,None,None,None,None,None),
    InlineKeyboardButton("B17-SB-01",Some("updated-b17-sb-01"),None,None,None,None,None,None),
    InlineKeyboardButton("B17-RO-01",Some("updated-b17-ro-01"),None,None,None,None,None,None),
    InlineKeyboardButton("B16-SE-01",Some("updated-b16-se-01"),None,None,None,None,None,None),
    InlineKeyboardButton("B16-RO-01",Some("updated-b16-ro-01"),None,None,None,None,None,None),
    InlineKeyboardButton("B16-DS-01",Some("updated-b16-ds-01"),None,None,None,None,None,None),
    InlineKeyboardButton("B16-DS-02",Some("updated-b16-ds-02"),None,None,None,None,None,None),
    InlineKeyboardButton("M19-SE-01",Some("updated-m19-se-01"),None,None,None,None,None,None),
    InlineKeyboardButton("M19-SE-02",Some("updated-m19-se-02"),None,None,None,None,None,None),
    InlineKeyboardButton("M19-DS-01",Some("updated-m19-ds-01"),None,None,None,None,None,None),
    InlineKeyboardButton("M19-DS-02",Some("updated-m19-ds-02"),None,None,None,None,None,None),
    InlineKeyboardButton("M19-RO-01",Some("updated-m19-ro-01"),None,None,None,None,None,None),
    InlineKeyboardButton("M18-RO-01",Some("updated-m18-ro-01"),None,None,None,None,None,None))


  "groupButtons" should "be created correctly - createGroupButtons" in {
     bot.groupButtons shouldBe groupButtons
   }

  "electiveButtons" should "be created correctly - createElectiveButtons" in {
    bot.electiveButtons shouldBe electiveButtons
  }

  "electiveButtons" should "be created correctly - createGroupUpdateButtons" in {
    bot.updatedGroupButton shouldBe updatedGroupButtons
  }

  "deleteAllReminders" should "not crash on empty lists" in {
    bot.usersSchedulers = List.empty :List[(Int, String, Cancelable)]
    bot.usersElectiveSchedulers = List.empty : List[(Int, String, List[(Cancelable, Cancelable)])]
    bot.deleteAllReminders()
    (bot.usersElectiveSchedulers, bot.usersSchedulers) shouldBe (List.empty, List.empty)
  }

  "deleteAllReminders" should "really delete elements from both lists lists" in {
    bot.usersSchedulers = List((1, "mew", scheduler.scheduleWithFixedDelay(1.minute, 5.minutes) {println("mew time")}),
      (2, "gav", scheduler.scheduleWithFixedDelay(1.minute, 5.minutes) {println("gav time")})) :List[(Int, String, Cancelable)]
    bot.usersElectiveSchedulers = List(
    (1, "elective 1",   List((scheduler.scheduleWithFixedDelay(1.minute, 5.minutes) {println("elective day1-a day before")},
        scheduler.scheduleWithFixedDelay(10.minute, 5.minutes) {println("elective day1 time")}),
      (scheduler.scheduleWithFixedDelay(20.minute, 5.minutes) {println("elective day2- a day before time")},
        scheduler.scheduleWithFixedDelay(30.minute, 5.minutes) {println("elective day2 time")}))))
      : List[(Int, String, List[(Cancelable, Cancelable)])]
    bot.deleteAllReminders()
    (bot.usersElectiveSchedulers, bot.usersSchedulers) shouldBe (List.empty, List.empty)
  }

//  we do need to check cases when the elective is not in scheduled because it is always checked before the function use
  "deleteElective" should "delete correctly" in {
    bot.usersElectiveSchedulers = List((1, "elective 1",
      List((scheduler.scheduleWithFixedDelay(1.minute, 5.minutes) {println("elective day1-a day before")},
        scheduler.scheduleWithFixedDelay(10.minute, 5.minutes) {println("elective day1 time")}),
        (scheduler.scheduleWithFixedDelay(20.minute, 5.minutes) {println("elective day2- a day before time")},
          scheduler.scheduleWithFixedDelay(30.minute, 5.minutes) {println("elective day2 time")}))),
      (1, "elective 2",
        List((scheduler.scheduleOnce(2.minutes) {println("elective 2 day1-a day before")},
          scheduler.scheduleOnce(2.minute ) {println("elective 2 day1 time")}),
          (scheduler.scheduleOnce(2.minute) {println("elective 2 day2- a day before time")},
            scheduler.scheduleOnce(2.minute) {println("elective 2 day2 time")})))
    ): List[(Int, String, List[(Cancelable, Cancelable)])]
    val res = List(bot.usersElectiveSchedulers(1))
    bot.deleteElective("elective 1", 1)
    bot.usersElectiveSchedulers shouldBe res
    bot.deleteAllReminders()
  }

  "setupCoursesAndLabs B19-03" should "work correctly" in {
    val tScheduler = TestScheduler()
    val localbot = new ScheduleBot("mew", "coreLink", "electiveLink",
      coreFile, electiveFile, db)(tScheduler)(Clock.systemDefaultZone)
    val cbq = CallbackQuery(id = "1285522135998800610",
      from = User(id=299308946, isBot = false, firstName = "cat", lastName = None, username = None, languageCode = None),
      message=None, inlineMessageId=None,
      chatInstance = "8722516864877688585",
      data=Some("B18-03"), gameShortName=None )
    MyTables.fillDatabase(db, coreFile)
    localbot.setupCoursesAndLabs("B19-03", cbq)

    val expectedTasks = 17
    val tasks = tScheduler.state.tasks.size
    val cores = db.run(localbot.courses.filter(_.id===299308946).result)
    Await.result(cores, 100.millis)
    val expectedCores = Vector(UsersCourse(299308946,"analytical geometry and linear algebra 2",false),
      UsersCourse(299308946,"data structure and algorithms",false),
      UsersCourse(299308946,"introduction to programming 2",false),
      UsersCourse(299308946,"mathematical analysis 2",false),
      UsersCourse(299308946,"theoretical computer science",false))
    (tasks, cores.value.get.get) shouldBe (expectedTasks, expectedCores)
  }

// schedulers will be tested in other place
  "setupCore" should "setup both db insert and reminders setup" in {
    bot.setupCore(Core("monday", "09:00", "introduction to ai", "brown", 105, None, None, None, None),
      "introduction to ai" , 1)
    val insertCheck = db.run(bot.courses.filter(_.id===1).result)
    Await.result(insertCheck, 100.millis)
    (bot.usersSchedulers.map(e=>(e._1, e._2)), insertCheck.value.get.get) shouldBe (List((1,"introduction to ai-lec")),
      Vector(UsersCourse(1, "introduction to ai", false)))
    bot.deleteAllReminders()
  }

//  case class Labs(weekday: String, time: String, courseName: String, ta: String, room: Int)
  "setupLab" should "returns one task that reloads every week" in {
    val tScheduler = TestScheduler()
    val localbot = new ScheduleBot("mew", "coreLink", "electiveLink",
      coreFile, electiveFile, db)(tScheduler) (Clock.fixed(Instant.parse("2020-03-02T10:00:00.0Z"),
      ZoneId.of("Europe/Moscow")))
    val c = localbot.setupLab((Parsing.Labs("monday", "15:45-17:10", "introduction to ai", "rufina", 313),
      Some("B19-03")), 1)
    val l1 = tScheduler.state.tasks.size
    tScheduler.tick(155.minutes)
    val l2 = tScheduler.state.tasks.size
    tScheduler.tick(7.days)
    val l3= tScheduler.state.tasks.size
    c.cancel()
    (l1, l2, l3) shouldBe (1,1,1)
  }

// setupElective sets up only schedulers
//  we do not need to tests for non-existing electives
//  as function is executed only after button with existing elective is pressed
  "setupElective for scala" should "set up 60 tasks for a semester and schedule then correctly" in {
    val tScheduler = TestScheduler()
    val localbot = new ScheduleBot("mew", "coreLink", "electiveLink",
      coreFile, electiveFile, db)(tScheduler)(Clock.fixed(Instant.parse("2020-01-10T10:00:00.0Z"),
      ZoneId.of("Europe/Moscow")))
    localbot.setupElective("functional programming and scala language", 1)
    val initialSize = tScheduler.state.tasks.size
    tScheduler.tick(13210.minutes)
    val dayBeforeSize = tScheduler.state.tasks.size
    tScheduler.tick(1440.minutes)
    val inDaySize = tScheduler.state.tasks.size

    (initialSize, dayBeforeSize, inDaySize) shouldBe (60, 59, 58)
  }

  "hasGroupTag B19-03 for groups" should "return instance" in {
    val mockCallback = CallbackQuery(id = "1285522135998800610",
      from = User(id=299308946, isBot = false, firstName = "cat", lastName = None, username = None, languageCode = None),
      message=None, inlineMessageId=None,
      chatInstance = "8722516864877688585",
      data=Some("B19-03"), gameShortName=None )
    bot.hasGroupTag(mockCallback, bot.groupTags) shouldBe Some("B19-03")
  }

  "hasGroupTag b10-05 for groups" should "return None" in {
    val mockCallback = CallbackQuery(id = "1285522135998800610",
      from = User(id=299308946, isBot = false, firstName = "cat", lastName = None, username = None, languageCode = None),
      message=None, inlineMessageId=None,
      chatInstance = "8722516864877688585",
      data=Some("B10-05"), gameShortName=None )
    bot.hasGroupTag(mockCallback, bot.groupTags) shouldBe None
  }

  "hasGroupTag abouts cats for electives" should "return None" in {
    val mockCallback = CallbackQuery(id = "1285522135998800610",
      from = User(id=299308946, isBot = false, firstName = "cat", lastName = None, username = None, languageCode = None),
      message=None, inlineMessageId=None,
      chatInstance = "8722516864877688585",
      data=Some("classes on why cats are great"), gameShortName=None )
    bot.hasGroupTag(mockCallback, bot.electiveTags) shouldBe None
  }

  "hasGroupTag b10-05 for electives" should "return instance" in {
    val mockCallback = CallbackQuery(id = "1285522135998800610",
      from = User(id=299308946, isBot = false, firstName = "cat", lastName = None, username = None, languageCode = None),
      message=None, inlineMessageId=None,
      chatInstance = "8722516864877688585",
      data=Some("advanced agile software design"), gameShortName=None )
    bot.hasGroupTag(mockCallback, bot.electiveTags) shouldBe Some("advanced agile software design")
  }

  "classesOnTheDay" should "work correctly" in {
    val cbq = CallbackQuery(id = "1285522135998800610",
      from = User(id=399308946, isBot = false, firstName = "cat", lastName = None, username = None, languageCode = None),
      message=None, inlineMessageId=None,
      chatInstance = "8722516864877688585",
      data=Some("B19-03"), gameShortName=None )
//
//    MyTables.fillDatabase(db, coreFile)
    bot.setupCoursesAndLabs("B18-03", cbq)
    val theirCourses = bot.courses.filter(_.id === 399308946).map(e => e.class_id).result
    val f = db.run(theirCourses)
    Await.result(f, 2.second)
    val res = f.value.get.get
    val todayClasses = bot.classesOnTheDay("monday", res)
    val expected = (List(("data modeling and databases 2","alexey kanatov","12:10-13:40",105),
      ("introduction to ai","joseph brown","09:00-10:30",105)),
      List(("data modeling and databases 2",Some("luiz araújo"),Some("14:10-15:40"),Some(105))),
      List(("introduction to ai","hamna aslam","15:45-17:15",303)))

    todayClasses shouldBe expected
  }

}
