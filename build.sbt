name := "myBot"

version := "0.1"

scalaVersion := "2.12.10"

libraryDependencies ++= Seq(
  "com.typesafe.slick"    %%    "slick"                % "2.1.0",
  "org.slf4j"             %     "slf4j-nop"            % "1.6.4",
  "com.typesafe.slick"    %%    "slick-hikaricp"       % "3.2.1",
  "org.xerial"            %     "sqlite-jdbc"          % "3.7.2",
  "com.github.pureconfig" %%    "pureconfig"           % "0.12.3",
  "com.bot4s"             %%    "telegram-core"        % "4.4.0-RC2",
  "org.scalaj"            %%    "scalaj-http"          % "2.4.2",
  "com.bot4s"             %%    "telegram-akka"        % "4.4.0-RC2",
  "io.monix"              %%    "monix"                % "3.0.0",
  "org.scalatest"         %%    "scalatest"            % "3.0.8" % Test,
  "org.postgresql"        %     "postgresql"           % "9.4-1206-jdbc42"
)