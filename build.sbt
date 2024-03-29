lazy val akkaHttpVersion = "10.1.8"
lazy val akkaVersion    = "2.6.0-M2"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.srfsoftware",
      scalaVersion    := "2.12.7"
    )),
    name := "scala-rest-service",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "com.typesafe.play" %% "play-json"            % "2.7.3",
      "com.typesafe.akka" %% "akka-http-caching"    % akkaHttpVersion,

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.5"         % Test
    )
  )

