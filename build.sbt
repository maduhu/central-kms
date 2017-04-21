name := "central-kms"

version := "1.0"

scalaVersion := "2.12.1"

val specs2Version = "3.8.6"
val akkaVersion = "2.4.17"
val akkaHttpVersion = "10.0.5"

val slickVersion = "3.2.0"
libraryDependencies ++= Seq(
  "net.i2p.crypto"      % "eddsa"                 % "0.2.0",
  "com.typesafe.akka"   %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka"   %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka"   %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.slick"  %% "slick"                % slickVersion,
  "com.typesafe.slick"  %% "slick-hikaricp"       % slickVersion exclude("com.zaxxer.HikariCP", "HikariCP"),
  "org.slf4j"           % "slf4j-nop"             % "1.6.4",
  "com.google.inject"   % "guice"                 % "4.1.0",
  "com.tzavellas"       %  "sse-guice"            % "0.7.1" exclude("com.google.inject", "guice"),
  "org.flywaydb"        % "flyway-core"           % "4.1.2",
  "org.postgresql"      % "postgresql"            % "42.0.0",
  "com.zaxxer"          % "HikariCP"              % "2.6.1",
  "org.specs2"          %% "specs2-core"          % specs2Version % Test,
  "org.specs2"          %% "specs2-mock"          % specs2Version % Test
)
    