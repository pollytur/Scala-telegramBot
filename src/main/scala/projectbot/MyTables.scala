package projectbot

import projectbot.Parsing.Core
import projectbot.Parsing.{Labs => LabsMock}
//import slick.driver.SQLiteDriver.api._
import slick.driver.PostgresDriver.api._

import scala.concurrent.Await
import scala.concurrent.duration._

// example from here
// https://github.com/yashsriv/sqlite-slick/blob/master/src/main/scala/com/gmantra/example/SqliteSlickExample.scala

object MyTables {

  case class User (id:Int, group_id:String)
  case class UsersCourse (id : Int, course_id : String, is_elective :Boolean)
  case class UsersLab (id : Int, lab_id : String, group_id: Option[String])

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Int]("id", O.PrimaryKey)
    //        def class_id = column[String]("class_id")
    def group_id = column[String]("group_id")
    def * = (id, group_id)<> (User.tupled, User.unapply )
  }

  class UserCourse(tag: Tag) extends Table[UsersCourse](tag, "user_course") {
    def id = column[Int]("id")
    def class_id = column[String]("class_id")
    def is_elective = column[Boolean]("is_elective")
    def pk = primaryKey("pk", (id, class_id))
    def * = (id, class_id, is_elective)<> (UsersCourse.tupled, UsersCourse.unapply )
  }

  class UserLab(tag: Tag) extends Table[UsersLab](tag, "user_lab") {
    def id = column[Int]("id")
    def lab_id = column[String]("lab_id")
    def group_id = column[Option [String]]("group_id")
    def pk = primaryKey("pk_2", (id, lab_id))
    def * = (id, lab_id, group_id )<> (UsersLab.tupled, UsersLab.unapply)
  }

  class CommonClasses(tag: Tag) extends
    Table[Core](tag, "classes") {
    def dayLecture = column[String]("weekday_lecture")
    def lectureTime = column[String]("time")
    def classId = column[String]("class_id", O.PrimaryKey)
    def prof = column[String]("prof")
    def lectureRoom = column[Int]("room")
    def dayTutorial = column[Option[String]]("weekday_tutorial")
    def tutorialTime = column[Option[String]]("tutorial_time")
    def tutorialTeacher = column[Option[String]]("tutorial_teacher")
    def tutorialRoom = column[Option[Int]]("tutorial_room")
    //      def hasLabs = column[Boolean]("has_labs")
    def * =
      (dayLecture, lectureTime, classId, prof,  lectureRoom,
        dayTutorial, tutorialTime, tutorialTeacher, tutorialRoom) <> (Core.tupled, Core.unapply)
  }

  // labs are other instance as there are many electives with only labs
  // and there are different number of labs for different courses
  // example for * from here
  // https://stackoverflow.com/questions/26504729/slick-transform-subset-of-table-columns-into-a-case-class
  class Labs(tag: Tag) extends Table[(LabsMock, Option[String])](tag, "labs") {
    def labDay = column[String]("lab_day")
    def labTime = column[String]("lab_time")
    def classId = column[String]("class_id")
    def ta = column[String]("ta")
    def labRoom = column[Int]("lab_room")
    def groupId = column[Option[String]]("group_id")
//    pk is such because there are courses which can have more than 1 lab and those labs may be at 1 day
    def pk = primaryKey("pk_3", (classId, groupId, labDay, labTime))
    def * = (labDay, labTime, classId, ta, labRoom, groupId).shaped <>
      ({case (labDay, labTime, classId, ta, labRoom, groupId) =>
        (LabsMock(labDay, labTime, classId, ta, labRoom), groupId)},
        {ct: (LabsMock, Option[String]) =>
          Some(ct._1.weekday, ct._1.time, ct._1.courseName, ct._1.ta, ct._1.room, ct._2)}
      )
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
        labs.schema.create,
        cores.schema.create,
        users.schema.create,
        courses.schema.create,
        userLabs.schema.create
      )
      val result = db.run(setup)
      Await.result(result, 15.seconds)
    }
  }

  def fillDatabase(db: Database, doc: String): Unit = {
    val labs = TableQuery[Labs]
    val cores = TableQuery[CommonClasses]
    val res = Parsing.fillingDatabase(doc)

    val toBeInserted = res._1.map { row => cores.insertOrUpdate(row) }
    val inOneGo = DBIO.sequence(toBeInserted)
    val dbioFuture = db.run(inOneGo)
    Await.result(dbioFuture, 3.second).sum

    val labstoBeInserted = res._2.flatMap { row => row._1.map { l => labs.insertOrUpdate((l, row._2)) } }
    val labsinOneGo = DBIO.sequence(labstoBeInserted)
    val labsdbioFuture = db.run(labsinOneGo)
    Await.result(labsdbioFuture, 3.second).sum
  }



}
