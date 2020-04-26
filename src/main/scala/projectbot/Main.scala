package projectbot

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate, LocalDateTime, Month, ZoneId}

import com.bot4s.telegram.models.{CallbackQuery, User}
import monix.execution.Cancelable
import projectbot.Time4Bot.timeTill

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import slick.driver.SQLiteDriver.api._
import pureconfig.ConfigSource
import pureconfig._
import pureconfig.generic.auto._

import scala.concurrent.duration._
import monix.execution.Scheduler.{global => scheduler}
import monix.execution.schedulers.TestScheduler
import projectbot.Parsing.Core


object Main extends App {
  val m = ConfigSource.default.load[BotConfig]
  m match {

    case Right(conf) => {
      //  Part with database

      val db = Database.forConfig("bot")
//
       MyTables.createTables(db)
       MyTables.fillDatabase(db, conf.coreFile)
//      val bot = new ScheduleBot(conf.token, conf.coreLink, conf.electiveLink, conf.coreFile, conf.electiveFile, db)(scheduler) (Clock.systemDefaultZone)
      val tScheduler = TestScheduler()
      val localbot = new ScheduleBot("mew", "coreLink", "electiveLink",
        conf.coreFile, conf.electiveFile, db)(tScheduler)(Clock.systemDefaultZone)
      val cbq = CallbackQuery(id = "1285522135998800610",
        from = User(id=299308946, isBot = false, firstName = "cat", lastName = None, username = None, languageCode = None),
        message=None, inlineMessageId=None,
        chatInstance = "8722516864877688585",
        data=Some("B18-03"), gameShortName=None )
      localbot.setupCoursesAndLabs("B19-03", cbq)
      val userGroup = db.run(localbot.users.filter(_.id===299308946).result)
      val expectedUserGroup = Vector(MyTables.User(299308946, "B18-03"))
      Await.result(userGroup, 100.millis)
      val expectedTasks = 18
      val tasks = tScheduler.state.tasks.size
      val cores = db.run(localbot.courses.filter(_.id===299308946).result)
      Await.result(cores, 100.millis)
      println(cores.value.get.get)
      val expectedCores = "mew"
//      //
//      //     To run spawn the bot
//      //  write me in telegram if token needed

//      val eol = bot.run()
//      println("Press [ENTER] to shutdown the bot, it may take a few seconds...")
//      scala.io.StdIn.readLine()
//      bot.shutdown() // initiate shutdown
//      // Wait for the bot end-of-life
//      Await.result(eol, Duration.Inf)
      db.close()
    }
    case Left(error) =>
      println("no config")
  }
}
