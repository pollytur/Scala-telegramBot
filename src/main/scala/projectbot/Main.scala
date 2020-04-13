package projectbot

import akka.actor.Status.Success
import projectbot.MyTables.UserCourse

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import slick.driver.SQLiteDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {
//  Part with database
  val db = Database.forConfig("bot")
  MyTables.createTables(db)
  MyTables.fillDatabase(db, "Core Courses_Spring_2019-2020 - BS,MS_Spring 2019.csv")
  println("Done")


//     To run spawn the bot
//  write me in telegram if token needed
//  todo create config with token
    val bot = new ScheduleBot("token",
      "https://docs.google.com/spreadsheets/d/1H3SYKtt1_E_kqJ9REG9hWAJskpSDTrRKHe6tSglv5_0/edit#gid=413604235",
      "https://docs.google.com/spreadsheets/d/1h0VhA48io0Z345gPtXVr7S1OTKolrA3JrjMeFhFLsQI/edit#gid=516269660",
      "Core Courses_Spring_2019-2020 - BS,MS_Spring 2019.csv",
      "Electives Schedule Spring 2020 Bachelors - Main.csv", db)
    val eol = bot.run()
    println("Press [ENTER] to shutdown the bot, it may take a few seconds...")
    scala.io.StdIn.readLine()
    bot.shutdown() // initiate shutdown
    // Wait for the bot end-of-life
    Await.result(eol, Duration.Inf)

  db.close()

}
