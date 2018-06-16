val scalaJsDomV = "0.9.5"
val akkaHttpV = "10.1.1"
val akkaV = "2.5.12"
val scalaV = "2.12.6"

lazy val commonSettings = Seq(version := "0.1", scalaVersion := scalaV)

lazy val frontend = project.in(file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % scalaJsDomV,
      "org.querki" %%% "jquery-facade" % "1.2"
    ),
    scalaJSUseMainModuleInitializer := true
  ).dependsOn(sharedJS)

lazy val backend = project.in(file("backend"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpV,
      "com.typesafe.akka" %% "akka-stream" % akkaV
    )
  )

lazy val shared = (crossProject.crossType(CrossType.Pure) in file ("shared"))
  .settings(
    scalaVersion := scalaV
  )

lazy val sharedJS = shared.js

lazy val root =
  project.in(file("."))
    .aggregate(frontend, backend)