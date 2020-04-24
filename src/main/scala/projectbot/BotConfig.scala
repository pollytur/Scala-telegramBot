package projectbot

import pureconfig._
import pureconfig.generic.auto._

case class BotConfig (
  token             : String,
  coreLink          : String,
  electiveLink      : String,
  coreFile          : String,
  electiveFile      : String,
)
