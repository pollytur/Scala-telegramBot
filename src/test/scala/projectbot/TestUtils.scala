package projectbot
// adapted from from https://github.com/bot4s/telegram/blob/master/core/test/src/com/bot4s/telegram/api/TestUtils.scala

import com.bot4s.telegram.models._


trait TestUtils {
  val botUser = User(123, false, "FirstName", username = Some("TestBot"))

  def textMessage(text: String): Message =
    Message(0, chat = Chat(0, ChatType.Private),from=Some(botUser), date = 0, text = Some(text))
}