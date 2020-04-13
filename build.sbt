name := "myBot"

version := "0.1"

scalaVersion := "2.12.10"

// Core with minimal dependencies, enough to spawn your first bot.
libraryDependencies += "com.bot4s" %% "telegram-core" % "4.4.0-RC2"
libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.4.2"

// Extra goodies: Webhooks, support for games, bindings for actors.
libraryDependencies += "com.bot4s" %% "telegram-akka" % "4.4.0-RC2"
//slick import
//libraryDependencies +="com.typesafe.slick" %% "slick" % "3.3.2"

libraryDependencies ++= Seq(
//  "org.postgresql" % "postgresql" % "9.3-1100-jdbc4",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4"
)

libraryDependencies ++= Seq(
//  "com.typesafe.slick"  %% "slick"                % "3.1.1",
  "com.typesafe.slick"  %% "slick-hikaricp"       % "3.2.1",
//  "org.slf4j"           %  "slf4j-nop"            % "1.6.4",
  "org.xerial"          %  "sqlite-jdbc"          % "3.7.2"
)