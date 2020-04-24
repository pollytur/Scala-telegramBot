package projectbot

import org.scalatest.{FlatSpec, Matchers}
import Time4Bot._

class Time4BotSpec extends FlatSpec with Matchers {

   "classTime" should "return 850" in {
     classTime("14:10") shouldBe 850
   }

//  "" should "" in {
//    shouldBe
//  }
}
