package projectbot

import java.time.{Clock, DayOfWeek, Instant, LocalDate, Month, ZoneId}

import org.scalatest.{FlatSpec, Matchers}
import Time4Bot._

class Time4BotSpec extends FlatSpec with Matchers {
//  it will be 13-00 because Moscow is UTC+3
  val clock = Clock.fixed(Instant.parse("2020-03-04T10:00:00.0Z"), ZoneId.of("Europe/Moscow"))

   "classTime" should "return 850" in {
    classTime("14:10") shouldBe 850
  }

  "todayDay" should "return sunday" in {
    todayDay(clock) shouldBe DayOfWeek.WEDNESDAY
  }

  "tillDayEnd" should "return 840" in {
    tillDayEnd(clock) shouldBe 660
  }

  "timeTill friday 14-10" should "return 2940" in {
    timeTill("friday", "14:10", clock) shouldBe 2940
  }

//  do not forget about -10 minutes offset for notification
  "timeTill wednesday 14-10" should "return 70" in {
    timeTill("wednesday", "14:10", clock) shouldBe 60
  }

  "timeTill wednesday 10-10" should "return 9900" in {
    timeTill("wednesday", "10:10", clock) shouldBe 9900
  }

  "timeTill monday 10:35" should "return 7045" in {
    timeTill("monday", "10:35", clock) shouldBe 7045
  }

  "timeBeforeElective 20-03-2020 " should "return Option(23100)" in {
    timeBeforeElective( LocalDate.of(2020, Month.MARCH, 20), "14:10", clock) shouldBe Some(23100)
  }

  "timeBeforeElective 04-03-2020 " should "return Option(23100)" in {
    timeBeforeElective( LocalDate.of(2020, Month.MARCH, 4), "14:10", clock) shouldBe Some(60)
  }

  "timeBeforeElective 01-02-2020" should "return None" in {
    timeBeforeElective( LocalDate.of(2020, Month.FEBRUARY, 20), "15:45", clock) shouldBe None
  }

}
