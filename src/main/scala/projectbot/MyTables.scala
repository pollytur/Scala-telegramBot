package projectbot

import slick.driver.SQLiteDriver.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import util.control.Breaks._

// example from here
// https://github.com/yashsriv/sqlite-slick/blob/master/src/main/scala/com/gmantra/example/SqliteSlickExample.scala

object MyTables {

  class Users(tag: Tag) extends Table[(Int, String)](tag, "users") {
    def id = column[Int]("id")

    //        def class_id = column[String]("class_id")
    def group_id = column[String]("group_id")

    def * = (id, group_id)
  }

  class UserCourse(tag: Tag) extends Table[(Int, String)](tag, "user_course") {
    def id = column[Int]("id")

    def class_id = column[String]("class_id")

    def * = (id, class_id)
  }

  class UserLab(tag: Tag) extends Table[(Int, String)](tag, "user_lab") {
    def id = column[Int]("id")

    def lab_id = column[String]("lab_id")

    def * = (id, lab_id)
  }


  class CommonClasses(tag: Tag) extends
    Table[(String, String, String, String, Int,
      Option[String], Option[String], Option[String], Option[Int])](tag, "classes") {
    def classId = column[String]("class_id")

    def prof = column[String]("prof")

    def dayLecture = column[String]("weekday_lecture")

    def lectureTime = column[String]("time")

    def lectureRoom = column[Int]("room")

    def tutorialTeacher = column[Option[String]]("tutorial_teacher")

    def dayTutorial = column[Option[String]]("weekday_tutorial")

    def tutorialTime = column[Option[String]]("tutorial_time")

    def tutorialRoom = column[Option[Int]]("tutorial_room")

    //      def hasLabs = column[Boolean]("has_labs")
    def * =
      (classId, prof, dayLecture, lectureTime, lectureRoom,
        tutorialTeacher, dayTutorial, tutorialTime, tutorialRoom)
  }

  // labs are other instance as there are many electives with only labs
  // and there are different number of labs for different courses
  class Labs(tag: Tag) extends Table[(String, Option[String], String, String, String, Int)](tag, "labs") {
    def classId = column[String]("class_id")

    def groupId = column[Option[String]]("group_id")

    def ta = column[String]("ta")

    def labDay = column[String]("lab_day")

    def labTime = column[String]("lab_time")

    def labRoom = column[Int]("lab_room")

    def * = (classId, groupId, ta, labDay, labTime, labRoom)
  }


  //  todo create if no one exists
  def createTables(db: Database): Unit = {
    val courses = TableQuery[UserCourse]
    val labs = TableQuery[Labs]
    val cores = TableQuery[CommonClasses]
    val users = TableQuery[Users]
    val userLabs = TableQuery[UserLab]
    try {
      val setup = DBIO.seq(
        (labs.schema).create,
        cores.schema.create,
        users.schema.create,
        courses.schema.create,
        userLabs.schema.create
      )
      val result = db.run(setup)
      Await.result(result, 2.second)
    }
  }

  //  todo fill the courses, labs and electives tables
  //  todo find how to insert a batch
  def fillDatabase(db: Database, doc: String): Unit = {
    val labs = TableQuery[Labs]
    val cores = TableQuery[CommonClasses]
    val groups = Parsing.groups(doc)
    for (gr <- groups) {
      breakable {
        //      bs17-ds-01 problems in add lectures
        if (gr.contains("-01")) {
          val intermediate = Parsing.addLectures(doc, gr)
          val customCourse = Parsing.groupLecturesAndTutorials(intermediate)
          for (c <- customCourse) {
            val insert = DBIO.seq(
              cores += ((c.lecName, c.lecturer, c.lecDay, c.lecTime, c.lecRoom, c.tutTeacher, c.tutDay, c.tutTime, c.tutRoom))
            )
            val result = db.run(insert)
            Await.result(result, 1.second)
          }
        }

        if (gr == "M19-SE-02" || gr == "M19-DS-02") break

        val lab = Parsing.onlyLabs(doc, gr)
        for (l <- lab) {
          val insert = DBIO.seq(
            labs += ((l.courseName, Some(gr), l.ta, l.weekday, l.time, l.room))
          )
          val result = db.run(insert)
          Await.result(result, 1.second)
        }
      }
    }
  }

}
