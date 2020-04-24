package projectbot

import java.time.{LocalDate, Month}


import scala.concurrent.Await
import scala.concurrent.duration.Duration
import slick.driver.SQLiteDriver.api._
import pureconfig.ConfigSource
import pureconfig._
import pureconfig.generic.auto._


object Main extends App {
  val m = ConfigSource.default.load[BotConfig]
  m match {

    case Right(conf) => {

      //  Part with database

      val db = Database.forConfig("bot")

      //      MyTables.createTables(db)
      //      MyTables.fillDatabase(db, conf.coreFile)
      //
      //      //     To run spawn the bot
      //      //  write me in telegram if token needed
      val bot = new ScheduleBot(conf.token, conf.coreLink, conf.electiveLink, conf.coreFile, conf.electiveFile, db)
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
