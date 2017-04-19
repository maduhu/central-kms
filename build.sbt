name := "central-kms"

version := "1.0"

scalaVersion := "2.12.1"

val specs2Version = "3.8.6"
val akkaVersion = "2.4.17"
val akkaHttpVersion = "10.0.5"

libraryDependencies ++= Seq(
  "net.i2p.crypto"      % "eddsa"                 % "0.2.0",
  "com.typesafe.akka"   %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka"   %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka"   %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.slick"  %% "slick"                % "3.2.0",
  "com.google.inject"   % "guice"                 % "4.1.0",
  "com.tzavellas"       %  "sse-guice"            % "0.7.1" exclude("com.google.inject", "guice"),
  "org.specs2"          %% "specs2-core"          % specs2Version % Test,
  "org.specs2"          %% "specs2-mock"          % specs2Version % Test
)
    