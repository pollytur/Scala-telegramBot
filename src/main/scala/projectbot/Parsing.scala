package projectbot

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date

import scala.io.Source
import scala.util.control.Breaks.{break, breakable}

object Parsing {
  val weekdays = List("monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday")
  val timePattern = "\\d\\d:\\d\\d-\\d\\d:\\d\\d"

  def isTime(str: String): Boolean = {
    str.matches(timePattern)
  }

  //  should be one of seven hardcoded values
  def isWeekday(str: String): Boolean = {
    weekdays.contains(str.toLowerCase())
  }

  //  string should contain 3 digits
  def isRoom(str: String): Boolean = {
    if ((str.length == 3) && (str forall Character.isDigit)) true
    else false
  }

  def isLecture(str: String): Boolean = {
    str.toLowerCase().contains("lec") || str.toLowerCase().contains("(*)")
  }

  def isTutorial(str: String): Boolean = {
    str.toLowerCase().contains("tutorial") || str.toLowerCase().contains("(tut.)")
  }

//  we do not check brakets or world lab here because there instances without all these
  def isLab(str: String): Boolean = {
    if (isLecture(str) || isTutorial(str)) false
    else true
  }

  def readData(fileName: String): Vector[Array[String]] = {
    for {
      line <- Source.fromFile(fileName).getLines().toVector
      //      values = line.split(",").map(_.trim)
      values = line.split(",")
    } yield values
  }

  def groups(fileName: String): List[String] = {
    readData(fileName)(1).drop(1).toList.filter(_ != "")
  }

//  return lectures and tutorials
//  it is done like this because lecture and tutorials may be in different dates,
//  so it is difficult to group them simultaneously
  def addLectures(fileName: String, group: String): List[String] = {
    val lectures = groupSubjects(fileName, group)
    var courses = List.empty[String]
    lectures match {
      case Right(v) =>
        var curDay = "MONDAY"
        for (i <- v.indices) {
          if (isWeekday(v(i))) curDay = v(i)
          if (isLecture(v(i)) || isTutorial(v(i))) courses = courses :+ curDay :+ v(i - 1) :+ v(i) :+ v(i + 1) :+ v(i + 2)
        }
    }
    courses
  }

  case class Core(lecDay: String, lecTime: String, lecName: String, lecturer: String, lecRoom: Int,
                  tutDay: Option[String], tutTime: Option[String], tutTeacher: Option[String], tutRoom: Option[Int]
                 )

  //  input for this function is output from addLectures, hence no labs inside the list
  def groupLecturesAndTutorials(classes: List[String]): List[Core] = {
    var grouped = List.empty[Core]
    for (i <- classes.indices) {
      if (isTime(classes(i))) {
        if (isLecture(classes(i + 1))) {
          val course_name = classes(i + 1).substring(0, classes(i + 1).indexOf("(")-1)
          val tut = classes.find(a => a.contains(course_name) && isTutorial(a))
          tut match {
            case Some(tut) => val tutIdx = classes.indexOf(tut)
              grouped = grouped :+ Core(classes(i - 1), classes(i), course_name, classes(i + 2), classes(i + 3).toInt,
                Some(classes(tutIdx - 2)), Some(classes(tutIdx - 1)), Some(classes(tutIdx + 1)), Some(classes(tutIdx + 2).toInt))

            case _ => grouped = grouped :+ Core(classes(i - 1), classes(i), course_name, classes(i + 2), classes(i + 3).toInt,
              None, None, None, None)
          }
        }
      }
    }
    grouped
  }

//  this function handles the peculiarities of the csv, when, for instance, different tracks have common lectures
  def conditionalFromCSV(fileName: String, group_intial: String, courses: List[String]): List[String] = {
    var coursesFin = courses
    if (group_intial.contains("B19") && group_intial != "B19-01") coursesFin = coursesFin ++ addLectures(fileName, "B19-01")
    if (group_intial.contains("B18") && group_intial != "B18-01") coursesFin = coursesFin ++ addLectures(fileName, "B18-01")
    if (group_intial.contains("B17")) {
      if (group_intial == "B17-SE-01" || group_intial == "B17-SB-01") {
        coursesFin = coursesFin :+ "TUESDAY" :+ "09:00-10:30" :+
          "Digital Signal Processing (Lec)" :+ "Nikolay Shilov" :+ "106" :+ "THURSDAY" :+ "10:35-12:05" :+ "Game theory (lec)" :+
          "Joseph Brown" :+ "106"
      }
      if (group_intial == "B17-RO-01") coursesFin = coursesFin :+ "TUESDAY" :+ "09:00-10:30" :+
        "Digital Signal Processing (Lec)" :+ "Nikolay Shilov" :+ "106"
      if (group_intial == "B17-DS-02") coursesFin = coursesFin ++ addLectures(fileName, "B17-DS-01")
      if (group_intial == "B17-SE-02") coursesFin = coursesFin ++ addLectures(fileName, "B17-SE-01")
    }
    if (group_intial.contains("B16")) {
      if (group_intial == "B16-DS-01") coursesFin = coursesFin :+ "TUESDAY" :+ "10:35-12:05" :+
        "Practical Machine Learning and Deep Learning (Lec)" :+ "Vladimir Ivanov" :+ "313" :+ "TUESDAY" :+ "12:10-13:40" :+
        "Practical Machine Learning and Deep Learning  (Lab)" :+ "Youssef  Ibrahim" :+ "300"

      if (group_intial == "B16-DS-02") coursesFin = coursesFin :+ "TUESDAY" :+ "10:35-12:05" :+
        "Practical Machine Learning and Deep Learning (Lec)" :+ "Vladimir Ivanov" :+ "313"

      if (group_intial != "B16-SE-01") coursesFin = coursesFin :+ "THURSDAY" :+ "17:20-18:50" :+ "Philosophy" :+ "Farida Nezhmetdinova" :+
        "106" :+ "THURSDAY" :+ "18:55-20:25" :+ "Philosophy" :+ "Farida Nezhmetdinova" :+ "106"
    }
    coursesFin
  }

// this will return all classes for a selected group including labs
  def groupLabs(file: Vector[Array[String]], ind: Int): List[String] = {
    var groupCourses = List.empty[String]
    var dayNeeded = 0
    var curDay = "MONDAY"
    var lineNum = 0
    for (line <- file) {
      lineNum += 1
      if (lineNum > 2) {
        if (line.length > ind) {
          val k = line(ind)
          if (!line(ind).isEmpty && !line(ind).toLowerCase.contains("reserve for elective courses")
            && !line(ind).toLowerCase.contains("*starting form the second week")
          ) {
            if (dayNeeded == 0) {
              groupCourses = groupCourses :+ curDay :+ line(0) :+ line(ind)
              dayNeeded = 1
            }
            else {
              if (dayNeeded == 1) {
                groupCourses = groupCourses :+ line(ind)
                dayNeeded = 2
              }
              else {
                dayNeeded = 0;
                groupCourses = groupCourses :+ line(ind)
              }
            }
          }
        }
        else {
          if (line.length > 0 && isWeekday(line(0)))
            curDay = line(0)
        }
      }
    }
    groupCourses
  }

  //  todo change string to error
  //  this returns all classes (labs,lectures,tutorials) for a selected group
  def groupSubjects(fileName: String, group_intial: String): Either[String, List[String]] = {
    //  :Vector[Array[String]]
    val file = readData(fileName)
    ////  returns the index of the group in the list, returns -1 if no such group exists
    var group = group_intial
    //    all classes are merged for them
    if (group_intial == "M19-SE-02") group = "M19-SE-01"
    if (group_intial == "M19-DS-02") group = "M19-DS-01"

    val ind = file(1).indexOf(group)
    ind match {
      case -1 => Left("there is no such group")
      case _ => var courses = groupLabs(file, ind)
        courses = conditionalFromCSV(fileName, group, courses)
        while (isWeekday(courses.last)) {
          courses = courses.dropRight(1)
        }
        courses = courses.map(e => e.toLowerCase())
        Right(courses.filter(_ != "").filter(_ != "\"").filter(_ != "reserve for elective courses").
          filter(_ != "*starting form the second week"))
    }
  }

  case class Labs(weekday: String, time: String, courseName: String, ta: String, room: Int)

  def onlyLabs(fileName: String, group_intial: String): List[Labs] = {
    val file = readData(fileName)
    var group = group_intial
    //    all classes are merged for them
    if (group_intial == "M19-SE-02") group = "M19-SE-01"
    if (group_intial == "M19-DS-02") group = "M19-DS-01"
    val ind = file(1).indexOf(group)
    var courses = groupLabs(file, ind)
    courses = courses.map(e => e.toLowerCase())
    courses = courses.filter(_ != "").filter(_ != "\"").filter(_ != "reserve for elective courses").
      filter(_ != "*starting form the second week")

    var filteredCourses = List.empty[Labs]

    if (group.contains("-01")) {
      for (i <- courses.indices) {
        if (isTime(courses(i)) && courses.length >= i + 3) {
          if (!isLecture(courses(i + 1)) && !isTutorial(courses(i + 1))) {
            var courseName = courses(i + 1)
            if (courses(i + 1).contains("(lab") || courses(i + 1).contains("(p")) {
              courseName = courseName.substring(0, courseName.indexOf("(")-1)
            }
            filteredCourses = filteredCourses :+ Labs(courses(i - 1), courses(i), courseName, courses(i + 2), courses(i + 3).toInt)
          }
        }
      }
    }
    else {
      for (i <- courses.indices) {
        if (isTime(courses(i)) && courses.length >= i + 3) {
          var courseName = courses(i + 1)
          if (courses(i + 1).contains("(lab") || courses(i + 1).contains("(p")) {
            courseName = courseName.substring(0, courseName.indexOf("(")-1)
          }
          filteredCourses = filteredCourses :+ Labs(courses(i - 1), courses(i), courseName, courses(i + 2), courses(i + 3).toInt)
        }
      }
    }
    filteredCourses
  }

//  first list is all unique classes
//  was not used in final bot implementation
  def uniqueCourses(lst: List[String], labsNeeded: Boolean): List[String] = {
    var unique = List.empty[String]
    for (i <- lst.indices) {
      if (isTime(lst(i)) && i<lst.length-1) {
        if (i<lst.length && isLecture(lst(i + 1))) {
          val lec = lst(i + 1).substring(0, lst(i + 1).indexOf("(")-1)
          if (!unique.contains(lec)) unique = unique :+ lec
        }
        else {
          if (i<lst.length && isTutorial(lst(i + 1))) {
            val tut = lst(i + 1).substring(0, lst(i + 1).indexOf("(")-1)
            if (!unique.contains(tut)) unique = unique :+ tut
          }
          //        not all labs contains this
          //        this is for sure the name of the class, not prof name due to preprocessing
          else {
            if (i<lst.length && labsNeeded) {
              if (lst(i + 1).contains("(lab)") || lst(i + 1).contains("(prs.)") || lst(i + 1).contains("(lab.)")) {
                val lab = lst(i + 1).substring(0, lst(i + 1).indexOf("(")-1)
                if (!unique.contains(lab)) unique = unique :+ lab
              }
              else {
                if (!unique.contains(lst(i + 1))) unique = unique :+ lst(i + 1)
              }
            }
          }
        }
      }
    }
    unique
  }


  def fillingDatabase(doc: String): (List[Core], List[(List[Labs], Option[String])]) = {
    var cores = List.empty: List[Core]
    var labs = List.empty: List[(List[Labs], Option[String])]
    val groups = Parsing.groups(doc)
    for (gr <- groups) {
      breakable {
        if (gr.contains("-01")) {
          val intermediate = Parsing.addLectures(doc, gr)
          val customCourse = Parsing.groupLecturesAndTutorials(intermediate)
          cores ++= customCourse
        }
//        if (gr == "M19-SE-02" || gr == "M19-DS-02") break
        val lab = Parsing.onlyLabs(doc, gr)
        labs :+= (lab, Some(gr))
      }
    }
    (cores, labs)
  }

  def listOfElectives(fileName: String): List[String] = {
    readData(fileName)(0).drop(4).toList
  }

  case class Elective(classId: String, date: LocalDate, weekday: String, time: String, teacher: String, room: Int)

  def toDate(date: String): LocalDate = {
    val df = DateTimeFormatter.ofPattern("dd/MM/yyyy ")
    LocalDate.parse(date, df)
  }

//  will return elective timetable
  def parseElectives(electiveName: String): List[Elective] = {
    var courses = List.empty[Elective]
    val mew = readData("Electives Schedule Spring 2020 Bachelors - Main.csv")
    val ind = mew(0).indexOf(electiveName)
    //  println(ind)
    ind match {
      case -1 => Left("there is no such elective")
      case _ =>
        var cur_line = List.empty[String]
        var lineNum = 0
        var shift = 0
        var breaked = false
        var dayOfWeek = "monday"
        var curDate = toDate("01/01/2020 ")
        for (line <- mew) {
          if (lineNum > 0) {
            cur_line = cur_line ++ line
            breakable {
              if (breaked && (line(0).charAt(line(0).length - 1) != '"')) {
                if (cur_line.length <= ind + shift) shift += 1
                break
              }
              if (line.last.charAt(0) == '"') {
                breaked = true
                if (cur_line.length <= ind + shift) shift += 1
                break
              }
              else breaked = false

              if (cur_line.head != "") curDate = toDate(cur_line.head)
              if (cur_line(1) != "") dayOfWeek = cur_line(1)

              if (cur_line.length > ind + shift) {
                if (!cur_line(ind + shift).isEmpty) {
                  val i = cur_line(ind + shift + 2).substring(0, 3).toInt
                  val el = Elective(cur_line(ind + shift).substring(1, cur_line(ind + shift).length ), curDate, dayOfWeek, cur_line(3),
                    cur_line(ind + shift + 1), i)
                  courses = courses :+ el
                }
              }
              cur_line = List.empty
              shift = 0
            }
          }
          else lineNum += 1
        }
    }
    courses
  }

}
