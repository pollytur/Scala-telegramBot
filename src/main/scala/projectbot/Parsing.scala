package projectbot

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
    str.toLowerCase().contains("tutorial")
  }

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
          val course_name = classes(i + 1).substring(0, classes(i + 1).indexOf("("))
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

  //
  def groupLabs(file: Vector[Array[String]], ind: Int): List[String] = {
    var groupCourses = List.empty[String]
    var dayNeeded = 0
    var curDay = "MONDAY"
    var lineNum = 0
    for (line <- file) {
      lineNum += 1
      if (lineNum > 2) {
        if (line.length > ind) {
          if (!line(ind).isEmpty && !line(ind).toLowerCase.contains("reserve for elective courses")) {
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

//error somewhere here
    if (group_intial.contains("-01")) {
      for (i <- courses.indices) {
        if (isTime(courses(i)) && courses.length>=i+3) {
          if (!isLecture(courses(i+1)) && !isTutorial(courses(i+1))) {
            var courseName = courses(i+1)
            if (courses(i+1).contains("(lab)"))  {
              courseName = courseName.substring(0, courseName.indexOf("("))
            }
            filteredCourses = filteredCourses :+ Labs(courses(i-1), courses(i), courseName, courses(i + 2), courses(i + 3).toInt)
          }
        }
      }
    }
    else{
      for (i <- courses.indices) {
        if (isTime(courses(i)) && courses.length>=i+3) {
          var courseName = courses(i+1)
          if (courses(i+1).contains("(lab)"))  {
            courseName = courseName.substring(0, courseName.indexOf("("))
          }
          filteredCourses = filteredCourses :+ Labs(courses(i-1), courses(i), courseName, courses(i + 2), courses(i + 3).toInt)
        }
      }
    }
    filteredCourses
  }

  //  first list is all unique classes, second is ones with labs
  def uniqueCourses(lst: List[String]): List[String] = {
    var unique = List.empty[String]
    for (i <- lst.indices) {
      if (isTime(lst(i))) {
        if (isLecture(lst(i + 1))) {
          val lec = lst(i + 1).substring(0, lst(i + 1).indexOf("("))
          if (!unique.contains(lec)) unique = unique :+ lec
        }
        else {
          if (isTutorial(lst(i + 1))) {
            val tut = lst(i + 1).substring(0, lst(i + 1).indexOf("("))
            if (!unique.contains(tut)) unique = unique :+ tut
          }
          //        not all labs contains this
          //        this is for sure the name of the class, not prof name due to preprocessing
          else {
            if (lst(i + 1).contains("(lab)") || lst(i + 1).contains("(prs.)") || lst(i + 1).contains("(lab.)")) {
              val lab = lst(i + 1).substring(0, lst(i + 1).indexOf("("))
              if (!unique.contains(lab)) unique = unique :+ lab
            }
            else {
              if (!unique.contains(lst(i + 1))) unique = unique :+ lst(i + 1)
            }
          }
        }
      }
    }
    unique
  }

  // todo from here not tested
  def listOfElectives(fileName: String): List[String] = {
    readData(fileName)(0).drop(3).toList
  }

  def dateAndTimeForElective(fileName: String, elective: String): Either[String, List[String]] = {
    val file = readData(fileName)
    val ind = file(0).indexOf(elective)
    ind match {
      case -1 => Left("there is no such elective")
      case _ => Right(List("mew", "gav"))
    }
  }

  //  todo - переделать, иногда по индeксам не те строки и аутпут в either
  //  просто прогони этот пример в мейне
  def parseElectives(): Unit = {
    var courses = List.empty[String]
    val mew = readData("Electives Schedule Spring 2020 Bachelors - Main.csv")
    val ind = mew(0).indexOf("Human Computer Interaction Design for AI")
    //  println(ind)
    ind match {
      case -1 => Left("there is no such elective")
      case _ =>
        var cur_line = List.empty[String]
        var shift = 0
        var breaked = false
        for (line <- mew) {
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

            if (cur_line.length > ind + shift) {
              if (!cur_line(ind + shift).isEmpty) {
                courses = courses :+ cur_line(ind + shift)
                println("IN INDEX" + cur_line(ind + shift).mkString)
              }
            }
            else {
              if (line.length == 3) {
                //              println("OUT INDEX" + line.mkString)
                courses = courses ++ cur_line
              }
            }
            cur_line = List.empty
            shift = 0
          }
        }
    }
  }


}
