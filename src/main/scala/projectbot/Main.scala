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
      val bot = new ScheduleBot(conf.token, conf.coreLink, conf.electiveLink, conf.coreFile, conf.electiveFile, db)(scheduler) (Clock.systemDefaultZone)

//      //
//      //     To run spawn the bot
//      //  write me in telegram if token needed

      val eol = bot.run()
      println("Press [ENTER] to shutdown the bot, it may take a few seconds...")
      scala.io.StdIn.readLine()
      bot.shutdown() // initiate shutdown
      // Wait for the bot end-of-life
      Await.result(eol, Duration.Inf)
      db.close()
    }
    case Left(error) =>
      println("no config")
  }
}
