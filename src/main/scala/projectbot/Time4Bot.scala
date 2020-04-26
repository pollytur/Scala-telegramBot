package projectbot

import java.time.DayOfWeek.valueOf
import java.time.{Clock, DayOfWeek, LocalDate, LocalDateTime, LocalTime}
import java.time.format.DateTimeFormatter

object Time4Bot {
  val notificationBefore = 10

  def todayDay(clock : Clock): DayOfWeek = {
    val df = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val today = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now(clock))
    LocalDate.parse(today, df).getDayOfWeek
  }

  def classTime(time: String): Int = {
    val df = DateTimeFormatter.ofPattern("HH:mm")
    val transformed = LocalTime.parse(time, df)
    transformed.getHour * 60 + transformed.getMinute
  }

  def tillDayEnd(clock : Clock): Int = {
    val today = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now(clock))
    60 * 24 - classTime(today)
  }

  //  time in minutes
  def timeTill(day: String, time: String, clock : Clock): Int = {
    val today = todayDay(clock).getValue
    val desired = DayOfWeek.valueOf(day.toUpperCase).getValue
    if (today <= desired) {
      if (today < desired) (desired-today-1) * 24 * 60 + tillDayEnd(clock) + classTime(time.substring(0,5)) - notificationBefore
      else {
        val now = classTime(DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now(clock)))
        val timeOfclass = classTime(time.substring(0, 5))
        if (now > timeOfclass) 24 * 7 * 60 - (now - timeOfclass) - notificationBefore
        else {
          if (timeOfclass - now <= notificationBefore) 0
          else timeOfclass - now - notificationBefore
        }
      }
    }
    else {
      (desired + valueOf("SUNDAY").getValue - today-1) * 24 * 60 + tillDayEnd(clock) + classTime(time) - notificationBefore
    }
  }

  def timeBeforeElective(date: LocalDate, time: String, clock : Clock): Option[Long] = {
    val df = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val todayInter = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now(clock))
    val today = LocalDate.parse(todayInter, df).toEpochDay
    if (date.toEpochDay - today <= 0) {
      if (date.toEpochDay - today < 0) None
      else {
        val now = classTime(DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now(clock)))
        val timeOfclass = classTime(time.substring(0, 5))
        if (now > timeOfclass) None
        else {
          if (timeOfclass - now < notificationBefore) Some(0)
          else Some(timeOfclass - now - notificationBefore)
        }
      }
    }
    else {
      Some((date.toEpochDay - today-1) * 60 * 24 + tillDayEnd(clock) + classTime(time.substring(0,5)) - notificationBefore)
    }
  }
}
