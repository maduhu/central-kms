name := "central-kms"

scalaVersion := "2.12.1"

val akkaVersion = "2.4.17"
val akkaHttpVersion = "10.0.5"

val slickVersion = "3.2.0"
val json4sVersion = "3.5.2"
libraryDependencies ++= Seq(
  "net.i2p.crypto"      % "eddsa"                 % "0.2.0",
  "com.typesafe.akka"   %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka"   %% "akka-http"            % akkaHttpVersion,
  "org.json4s"          %% "json4s-native"        % json4sVersion,
  "org.json4s"          %% "json4s-ext"           % json4sVersion,
  "de.heikoseeberger"   %% "akka-http-json4s"     % "1.16.1",
  "com.typesafe.slick"  %% "slick"                % slickVersion,
  "com.typesafe.slick"  %% "slick-hikaricp"       % slickVersion exclude("com.zaxxer.HikariCP", "HikariCP"),
  "org.slf4j"           % "slf4j-nop"             % "1.6.4",
  "com.google.inject"   % "guice"                 % "4.1.0",
  "com.tzavellas"       %  "sse-guice"            % "0.7.1" exclude("com.google.inject", "guice"),
  "net.codingwell"      % "scala-guice_2.12"      % "4.1.0",
  "org.flywaydb"        % "flyway-core"           % "4.1.2",
  "org.postgresql"      % "postgresql"            % "42.0.0",
  "com.zaxxer"          % "HikariCP"              % "2.6.1",
  "org.scalatest"       %% "scalatest"            % "3.0.3" % Test,
  "com.typesafe.akka"   %% "akka-http-testkit"    % akkaHttpVersion % Test,
  "org.mockito"         % "mockito-core"          % "2.8.9" % Test
)
    
scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked")

logBuffered in Test := false
parallelExecution in Test := false
enablePlugins(JavaServerAppPackaging)
enablePlugins(DockerPlugin)
enablePlugins(AshScriptPlugin)
enablePlugins(GitVersioning)

packageName in Docker := "leveloneproject/central-kms"
dockerBaseImage := "openjdk:8-jdk-alpine"
dockerExposedPorts := Seq(8080)
dockerUpdateLatest := true
